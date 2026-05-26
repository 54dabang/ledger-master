package com.ledger.framework.web.service;

import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import com.ledger.common.constant.CacheConstants;
import com.ledger.common.constant.Constants;
import com.ledger.common.constant.UserConstants;
import com.ledger.common.core.domain.entity.SysUser;
import com.ledger.common.core.domain.model.LoginUser;
import com.ledger.common.core.redis.RedisCache;
import com.ledger.common.exception.ServiceException;
import com.ledger.common.exception.user.BlackListException;
import com.ledger.common.exception.user.CaptchaException;
import com.ledger.common.exception.user.CaptchaExpireException;
import com.ledger.common.exception.user.UserNotExistsException;
import com.ledger.common.exception.user.UserPasswordNotMatchException;
import com.ledger.common.utils.DateUtils;
import com.ledger.common.utils.MessageUtils;
import com.ledger.common.utils.StringUtils;
import com.ledger.common.utils.ip.IpUtils;
import com.ledger.framework.manager.AsyncManager;
import com.ledger.framework.manager.factory.AsyncFactory;
import com.ledger.framework.security.context.AuthenticationContextHolder;
import com.ledger.system.service.ISysConfigService;
import com.ledger.system.service.ISysUserService;

/**
 * 登录校验方法
 * 
 * @author ledger
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;


    /**
     * 登录验证
     * 
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {
        // 验证码校验
        validateCaptcha(username, code, uuid);
        // 登录前置校验
        loginPreCheck(username, password);
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createLoginToken(loginUser);
    }

    /**
     * 通过用户名获取token
     *
     * 这个方法主要给插件/白名单接口按用户名换取系统访问token使用。
     * 整体原则是：优先复用Redis中已有且仍有效的登录态，只刷新有效期；
     * 只有确认没有可复用登录态时，才重新构造LoginUser并创建新的登录态。
     *
     * @param loginName 用户名
     * @return token字符串
     */
    public String getTokenByLoginName(String loginName) {
        // 1. 校验用户名是否为空
        if (StringUtils.isEmpty(loginName)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(loginName, Constants.LOGIN_FAIL, MessageUtils.message("user.not.exists")));
            throw new UserNotExistsException();
        }

        /*
         * 2. 先尝试复用Redis里已经存在的有效token。
         *
         * tokenService.getValidTokenByUsername(loginName) 内部会：
         * 1) 通过 username -> uuid 的Redis索引查找登录态；
         * 2) 如果索引中出现多个uuid，说明登录态不唯一，会清理历史登录缓存并返回null；
         * 3) 如果索引缺失，再扫描历史登录缓存，避免旧缓存有效但索引不存在时误创建新token；
         * 4) 如果找到唯一有效LoginUser，只调用 refreshToken(loginUser) 重置Redis有效期；
         * 5) 使用原来的uuid重新签出JWT字符串返回。
         *
         * 注意：这里返回时不会生成新的uuid，也不会新增登录缓存。
         */
        String validToken = tokenService.getValidTokenByUsername(loginName);
        if (StringUtils.isNotEmpty(validToken)) {
            return validToken;
        }

        /*
         * 3. 走到这里表示Redis中没有找到可复用的有效登录态。
         * 此时才从数据库查询用户，并基于当前用户信息、权限信息创建新的LoginUser。
         */
        SysUser sysUser = userService.selectUserByUserName(loginName);
        UserDetails userDetails  = userDetailsService.createLoginUser(sysUser);

        /*
         * 4. 更新数据库中的最近登录信息，比如登录IP、登录时间。
         * 这一步不负责刷新Redis token有效期；Redis有效期由TokenService.refreshToken处理。
         */
        recordLoginInfo(sysUser.getUserId());

        /*
         * 5. 创建新的登录态。
         *
         * createLoginToken(loginUser) 会生成新的uuid，写入 loginUser.token，
         * 再把LoginUser缓存到 Redis key: legder:login_tokens:{uuid}，
         * 最后基于该uuid签发JWT字符串。
         *
         * 只有在前面确认没有有效旧token时，才会执行到这里。
         */
        LoginUser loginUser = (LoginUser) userDetails;
        String token = tokenService.createLoginToken(loginUser);

        /*
         * 6. 维护 username -> uuid 的索引。
         * 下次按用户名获取token时，可以先通过这个索引快速找到登录态并续期。
         * 同时清理该用户名下旧uuid对应的缓存，避免同一用户保留多份旧登录态。
         */
        tokenService.saveLatestUuidAndDeleteOldUuid(loginUser.getToken(), loginUser.getUsername());
        return token;
    }


    /**
     * 校验验证码
     * 
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
            String captcha = redisCache.getCacheObject(verifyKey);
            if (captcha == null)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
                throw new CaptchaExpireException();
            }
            redisCache.deleteObject(verifyKey);
            if (!code.equalsIgnoreCase(captcha))
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
                throw new CaptchaException();
            }
        }
    }

    /**
     * 登录前置校验
     * @param username 用户名
     * @param password 用户密码
     */
    public void loginPreCheck(String username, String password)
    {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("not.null")));
            throw new UserNotExistsException();
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
            throw new UserPasswordNotMatchException();
        }
        // IP黑名单校验
        String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr()))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("login.blocked")));
            throw new BlackListException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr());
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
