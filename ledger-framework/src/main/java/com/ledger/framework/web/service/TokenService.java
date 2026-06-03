package com.ledger.framework.web.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ledger.common.constant.CacheConstants;
import com.ledger.common.constant.Constants;
import com.ledger.common.core.domain.model.LoginUser;
import com.ledger.common.core.redis.RedisCache;
import com.ledger.common.utils.ServletUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.common.utils.ip.AddressUtils;
import com.ledger.common.utils.ip.IpUtils;
import com.ledger.common.utils.uuid.IdUtils;
import eu.bitwalker.useragentutils.UserAgent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * token验证处理
 *
 * @author ledger
 */
@Component
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    // 令牌自定义标识
    @Value("${token.header}")
    private String header;

    // 令牌秘钥
    @Value("${token.secret}")
    private String secret;

    // 令牌有效期（默认30分钟）
    @Value("${token.expireTime}")
    private int expireTime;

    protected static final long MILLIS_SECOND = 1000;

    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;

    private static final Long MILLIS_MINUTE_TWENTY = 20 * 60 * 1000L;

    @Autowired
    private RedisCache redisCache;

    /**
     * 获取用户身份信息
     *
     * @return 用户信息
     */
    public LoginUser getLoginUser(HttpServletRequest request) {
        // 获取请求携带的令牌
        String token = getToken(request);
        return getLoginUserByToken(token);
    }

    public LoginUser getLoginUserByToken(String token){
        if (StringUtils.isNotEmpty(token)) {
            try {
                Claims claims = parseToken(token);
                // 解析对应的权限以及用户信息
                String uuid = (String) claims.get(Constants.LOGIN_USER_KEY);
                String userKey = getLoginTokenKey(uuid);
                LoginUser user = redisCache.getCacheObjectIfValue(userKey);
                return user;
            } catch (Exception e) {
                log.error("获取用户信息异常，token:{}",token, e);
            }
        }
        return null;
    }

    /**
     * 设置用户身份信息
     */
    public void setLoginUser(LoginUser loginUser) {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    /**
     * 删除用户身份信息
     */
    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getLoginTokenKey(token);
            LoginUser loginUser = redisCache.getCacheObjectIfValue(userKey);
            redisCache.deleteObject(userKey);
            if (loginUser != null) {
                deleteUsernameUuidIndexIfMatches(loginUser.getUsername(), token);
            }
        }
    }

    /**
     * 创建令牌
     *
     * 这里会创建新的登录态uuid，并写入Redis登录缓存。
     * 如果只是给已有token续期，不应该调用该方法，而应该调用refreshToken。
     *
     * @param loginUser 用户信息
     * @return 令牌
     */
    public String createLoginToken(LoginUser loginUser) {
        String uuid = IdUtils.fastUUID();
        return createLoginToken(loginUser, uuid);
    }
    public String createLoginToken(LoginUser loginUser,String uuid) {
        if(StringUtils.isEmpty(uuid)){
            uuid = IdUtils.fastUUID();
        }

        loginUser.setToken(uuid);
        setUserAgent(loginUser);
        refreshToken(loginUser);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, uuid);
        claims.put(Constants.JWT_USERNAME, loginUser.getUsername());
        return createJwtToken(claims);
    }

    /**
     * 根据用户名获取仍有效的令牌，并刷新令牌有效期。
     *
     * 先通过 username -> uuid 的索引查找；如果索引中出现多个uuid，说明登录态不唯一，
     * 会先清理历史登录缓存并返回null。只有索引唯一或索引缺失时，才继续尝试复用有效缓存。
     * 找到有效登录态后只刷新Redis有效期，不创建新uuid。
     *
     * @param username 用户名
     * @return 有效令牌，不存在时返回null
     */
    public String getValidTokenByUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            return null;
        }

        List<Object> usernameUuids = getUsernameUuids(username);
        if (hasMultipleUsernameUuids(usernameUuids)) {
            invalidateHistoricalLoginTokens(username, usernameUuids);
            return null;
        }

        String token = getValidTokenByUsernameUuids(username, usernameUuids);
        if (StringUtils.isNotEmpty(token)) {
            return token;
        }

        return getValidTokenByUsernameScan(username);
    }

    private boolean hasMultipleUsernameUuids(List<?> usernameUuids) {
        if (usernameUuids == null || usernameUuids.isEmpty()) {
            return false;
        }

        int uuidCount = 0;
        for (Object usernameUuid : usernameUuids) {
            if (usernameUuid instanceof String && StringUtils.isNotEmpty((String) usernameUuid)) {
                uuidCount++;
                if (uuidCount > 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private void invalidateHistoricalLoginTokens(String username, List<?> usernameUuids) {
        if (usernameUuids != null) {
            for (Object usernameUuid : usernameUuids) {
                if (usernameUuid instanceof String && StringUtils.isNotEmpty((String) usernameUuid)) {
                    redisCache.deleteObject(getLoginTokenKey((String) usernameUuid));
                }
            }
        }

        /*
         * 多个uuid说明该用户登录态已不唯一，这时不再挑选其中一个复用。
         * 同时扫描并清理该用户名残留的LoginUser缓存，确保上层重新创建干净的唯一登录态。
         */
        redisCache.scan(CacheConstants.LOGIN_TOKEN_KEY + "*", 1000, key -> {
            if (isUsernameUuidIndexKey(key, username)) {
                return;
            }

            Object cacheObject = redisCache.getCacheObjectIfValue(key);
            if (cacheObject instanceof LoginUser
                    && StringUtils.equals(username, ((LoginUser) cacheObject).getUsername())) {
                redisCache.deleteObject(key);
            }
        });
        deleteUsernameUuidIndex(username);
    }

    private String getValidTokenByUsernameUuids(String username, List<?> usernameUuids) {
        if (usernameUuids == null || usernameUuids.isEmpty()) {
            return null;
        }

        // 优先按用户名索引里记录的uuid查找，这是正常情况下最快的路径。
        for (Object usernameUuid : usernameUuids) {
            if (!(usernameUuid instanceof String)) {
                continue;
            }

            String token = refreshAndCreateTokenIfValid(username, (String) usernameUuid);
            if (StringUtils.isNotEmpty(token)) {
                return token;
            }
        }

        return null;
    }

    private String getValidTokenByUsernameScan(String username) {
        final List<String> matchedUuids = new ArrayList<>();
        /*
         * 兼容历史数据：如果旧登录缓存存在，但当时没有写入 username -> uuid 索引，
         * 仅靠索引会误判为没有有效token。这里用SCAN兜底查找该用户名的LoginUser。
         */
        redisCache.scan(CacheConstants.LOGIN_TOKEN_KEY + "*", 1000, key -> {
            if (isUsernameUuidIndexKey(key, username)) {
                return;
            }

            Object cacheObject = redisCache.getCacheObjectIfValue(key);
            if (!(cacheObject instanceof LoginUser)) {
                return;
            }

            LoginUser loginUser = (LoginUser) cacheObject;
            if (!StringUtils.equals(username, loginUser.getUsername())) {
                return;
            }

            matchedUuids.add(loginUser.getToken());
        });

        if (matchedUuids.size() > 1) {
            invalidateHistoricalLoginTokens(username, matchedUuids);
            return null;
        }

        if (matchedUuids.size() == 1) {
            return refreshAndCreateTokenIfValid(username, matchedUuids.get(0));
        }

        return null;
    }

    private String refreshAndCreateTokenIfValid(String username, String uuid) {
        // uuid对应的LoginUser还存在，才说明Redis登录缓存仍在有效期内。
        LoginUser loginUser = redisCache.getCacheObjectIfValue(getLoginTokenKey(uuid));
        if (loginUser == null || !StringUtils.equals(username, loginUser.getUsername())) {
            return null;
        }

        // 再校验LoginUser里记录的业务过期时间，避免只看Redis key存在导致误判。
        Long loginUserExpireTime = loginUser.getExpireTime();
        if (loginUserExpireTime == null || loginUserExpireTime <= System.currentTimeMillis()) {
            redisCache.deleteObject(getLoginTokenKey(uuid));
            return null;
        }

        // 复用原uuid刷新Redis有效期，并补齐/更新 username -> uuid 索引。
        refreshToken(loginUser);
        saveLatestUuidAndDeleteOldUuid(uuid, username);

        // 这里只是基于原uuid重新签发JWT字符串，不会创建新的Redis登录缓存。
        Map<String, Object> claims = new HashMap<>();
        claims.put(Constants.LOGIN_USER_KEY, uuid);
        claims.put(Constants.JWT_USERNAME, username);
        return createJwtToken(claims);
    }


    /**
     * 验证令牌有效期，相差不足20分钟，自动刷新缓存
     *
     * @param loginUser 登录信息
     * @return 令牌
     */
    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TWENTY) {
            refreshToken(loginUser);
        }
    }

    /**
     * 刷新令牌有效期
     *
     * @param loginUser 登录信息
     */
    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        // 根据uuid将loginUser缓存
        String userKey = getLoginTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    /**
     * 设置用户代理信息
     *
     * @param loginUser 登录信息
     */
    public void setUserAgent(LoginUser loginUser) {
        UserAgent userAgent = UserAgent.parseUserAgentString(ServletUtils.getRequest().getHeader("User-Agent"));
        String ip = IpUtils.getIpAddr();
        loginUser.setIpaddr(ip);
        loginUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
        loginUser.setBrowser(userAgent.getBrowser().getName());
        loginUser.setOs(userAgent.getOperatingSystem().getName());
    }

    /**
     * 从数据声明生成令牌
     *
     * @param claims 数据声明
     * @return 令牌
     */
    private String createJwtToken(Map<String, Object> claims) {
        String token = Jwts.builder()
                .setClaims(claims)
                .signWith(SignatureAlgorithm.HS512, secret).compact();
        return token;
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token 令牌
     * @return 数据声明
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 获取请求token
     *
     * @param request
     * @return token
     */
    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getLoginTokenKey(String uuid) {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }

    private String getUsernameTokenKey(String username) {
        return CacheConstants.LOGIN_TOKEN_USERNAME_KEY + username;
    }

    private String getLegacyUsernameTokenKey(String username) {
        return CacheConstants.LOGIN_TOKEN_KEY + username;
    }

    private List<Object> getUsernameUuids(String username) {
        List<Object> usernameUuids = new ArrayList<>();
        addUsernameUuids(usernameUuids, redisCache.getCacheListIfList(getUsernameTokenKey(username)));
        addUsernameUuids(usernameUuids, redisCache.getCacheListIfList(getLegacyUsernameTokenKey(username)));
        return usernameUuids;
    }

    private void addUsernameUuids(List<Object> usernameUuids, List<?> cachedUuids) {
        if (cachedUuids == null || cachedUuids.isEmpty()) {
            return;
        }
        for (Object cachedUuid : cachedUuids) {
            if (!usernameUuids.contains(cachedUuid)) {
                usernameUuids.add(cachedUuid);
            }
        }
    }

    private boolean isUsernameUuidIndexKey(String key, String username) {
        return StringUtils.equals(key, getUsernameTokenKey(username))
                || StringUtils.equals(key, getLegacyUsernameTokenKey(username));
    }

    private void deleteUsernameUuidIndex(String username) {
        redisCache.deleteObject(getUsernameTokenKey(username));
        String legacyUsernameTokenKey = getLegacyUsernameTokenKey(username);
        if (redisCache.isCacheList(legacyUsernameTokenKey)) {
            redisCache.deleteObject(legacyUsernameTokenKey);
        }
    }

    private void deleteUsernameUuidIndexIfMatches(String username, String uuid) {
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(uuid)) {
            return;
        }

        List<Object> usernameUuids = getUsernameUuids(username);
        for (Object usernameUuid : usernameUuids) {
            if (usernameUuid instanceof String && StringUtils.equals(uuid, (String) usernameUuid)) {
                deleteUsernameUuidIndex(username);
                return;
            }
        }
    }

    public void saveLatestUuidAndDeleteOldUuid(String uuid, String username) {
        List<Object> usernameUuids = getUsernameUuids(username);
        for (Object usernameUuid : usernameUuids) {
            if (usernameUuid instanceof String && !StringUtils.equals(uuid, (String)usernameUuid)) {
                redisCache.deleteObject(getLoginTokenKey((String) usernameUuid));
            }
        }

        deleteUsernameUuidIndex(username);

        String usernameTokenKey = getUsernameTokenKey(username);
        redisCache.setCacheList(usernameTokenKey, Arrays.asList(uuid));
        redisCache.expire(usernameTokenKey, expireTime, TimeUnit.MINUTES);
    }

}
