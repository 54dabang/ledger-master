package com.ledger.business.controller;

import com.alibaba.fastjson2.JSON;

import com.ledger.business.dto.Oauth2SsoLogin;
import com.ledger.common.constant.Constants;
import com.ledger.common.core.controller.BaseController;
import com.ledger.common.core.domain.AjaxResult;
import com.ledger.common.core.domain.model.LoginUser;
import com.ledger.common.utils.MessageUtils;
import com.ledger.common.utils.spring.SpringUtils;
import com.ledger.framework.manager.AsyncManager;
import com.ledger.framework.manager.factory.AsyncFactory;
import com.ledger.framework.security.context.AuthenticationContextHolder;
import com.ledger.framework.security.oauth2sso.Oauth2SsoUserInfo;
import com.ledger.framework.security.oauth2sso.Oauth2ssoAuthenticationToken;
import com.ledger.framework.web.service.SysLoginService;
import com.ledger.framework.web.service.TokenService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/oauth2sso")
public class Oauth2SsoController extends BaseController {

    @Value("${oauth2.client_id}")
    private String client_id;
    @Value("${oauth2.client_secret}")
    private String client_secret;
    @Value("${oauth2.redirect_uri}")
    private String redirect_uri;
    @Value("${oauth2.accesstoken_uri}")
    private String accesstoken_uri;
    @Value("${oauth2.userinfo_uri}")
    private String userinfo_uri;

    @Value("${url.mySystemHomeUrl}")
    private String mySystemHomeUrl;
    @Value("${url.oauth2SsoUrl}")
    private String oauth2SsoUrl;
    
    @Value("${oauth2.authorize_uri}")
    private String authorizeUri;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenService tokenService;

    /**
     * 收到授权码后，根据授权码换token, 然后根据token获取用户信息，然后调用spring
     * security登录，然后将生成jwt并将用户信息放到redis中，然后重定向到系统首页并将cookie写入浏览器。
     * 
     * @param request
     * @throws IOException
     */
    @GetMapping("/code")
    public void code(HttpServletRequest request, HttpServletResponse response, String code) throws IOException {
        if (StringUtils.isBlank(code)) {
//            AjaxResult ajaxResult = AjaxResult.error("code为空，无法进行登录");
//            ServletUtils.renderString(response, JSON.toJSONString(ajaxResult));
            response.sendRedirect(authorizeUri);
            return;
        }

        // 重定向到前端vue
        response.sendRedirect(oauth2SsoUrl + "?code=" + code);
    }

    @PostMapping("/login")
    public AjaxResult login(@RequestBody Oauth2SsoLogin oauth2SsoLogin) {
        String code = oauth2SsoLogin.getCode();
        if (StringUtils.isBlank(code)) {
            return AjaxResult.error("code为空，无法进行登录");
        }
        log.info("client_id--->>" + client_id + "---client_secret-->" + client_secret + "--redirect_uri-->"
                + redirect_uri + "--accesstoken_uri-->" + accesstoken_uri);

        HttpPost accessTokenHttpPost = new HttpPost(
                String.format(accesstoken_uri, client_id, "authorization_code", code, client_secret));
        try {
            // first: 根据授权码换token
            String accessTokenJson = EntityUtils
                    .toString(HttpClientBuilder.create().build().execute(accessTokenHttpPost).getEntity());
            Map<String, Object> accessTokenMap = strToMap(accessTokenJson);
            String accessToken = accessTokenMap.getOrDefault("access_token", "").toString();
            String uid = accessTokenMap.getOrDefault("uid", "").toString();
            // String refreshToken = accessTokenMap.getOrDefault("refresh_token",
            // "").toString();
            if (StringUtils.isBlank(accessToken) || StringUtils.isBlank(uid)) {
                AjaxResult ajaxResult = AjaxResult.error("accessToken或udi为空，无法进行登录, " + accessTokenJson);
                return ajaxResult;
            }

            // second: 根据token获取用户信息
            logger.info("access_token-->" + accessToken + ", uId-->" + uid + ", client_id-->" + client_id
                    + ", userinfo_uri-->" + userinfo_uri);
            HttpGet getUserInfoUrlRequest = new HttpGet(String.format(userinfo_uri, accessToken, client_id, uid));
            String userInfoJson = EntityUtils
                    .toString(HttpClientBuilder.create().build().execute(getUserInfoUrlRequest).getEntity());
            Map<String, Object> userInfoMap = strToMap(userInfoJson);
            String errcodeUserInfo = accessTokenMap.getOrDefault("errcode", "").toString();
            if (StringUtils.isNotBlank(errcodeUserInfo)) {
                AjaxResult ajaxResult = AjaxResult.error("根据accessToken获取用户信息失败, " + userInfoJson);
                return ajaxResult;
            }
            Oauth2SsoUserInfo oauth2SsoUserInfo = convertToUserInfo(userInfoMap);
            oauth2SsoUserInfo.setUid(uid);

            // third: 然后调用spring security登录
            // 用户验证
            Authentication authentication = null;
            try {
                Oauth2ssoAuthenticationToken authenticationToken = new Oauth2ssoAuthenticationToken(
                        oauth2SsoUserInfo.getLoginName());
                authenticationToken.setOauth2SsoUserInfo(oauth2SsoUserInfo);
                authenticationToken.setAccessToken(accessToken);
                AuthenticationContextHolder.setContext(authenticationToken);
                // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
                authentication = authenticationManager.authenticate(authenticationToken);
            } catch (Exception e) {
                log.error("单点登录 spring security登录失败", e);
                if (e instanceof BadCredentialsException) {
                    AsyncManager.me().execute(AsyncFactory.recordLogininfor(oauth2SsoUserInfo.getLoginName(),
                            Constants.LOGIN_FAIL, "单点登录: " + MessageUtils.message("user.password.not.match")));
                    AjaxResult ajaxResult = AjaxResult.error("用户还未同步到此业务系统, 无法登录");
                    return ajaxResult;
                } else {
                    AsyncManager.me().execute(AsyncFactory.recordLogininfor(oauth2SsoUserInfo.getLoginName(),
                            Constants.LOGIN_FAIL, "单点登录: " + e.getMessage()));
                    AjaxResult ajaxResult = AjaxResult.error("单点登录内部出错，请联系管理员" + e.getMessage());
                    return ajaxResult;
                }
            } finally {
                AuthenticationContextHolder.clearContext();
            }
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(oauth2SsoUserInfo.getLoginName(),
                    Constants.LOGIN_SUCCESS, "单点登录: " + MessageUtils.message("user.login.success")));
            LoginUser loginUser = (LoginUser) authentication.getPrincipal();
            SysLoginService sysLoginService = SpringUtils.getBean(SysLoginService.class);
            sysLoginService.recordLoginInfo(loginUser.getUserId());

            // fourth: 然后将生成jwt并将用户信息放到redis中
            String jwtToken = tokenService.createToken(loginUser);
            
            // 记录下本次登录uuid,删除所有之前登录生成的uuid
            tokenService.saveLatestUuidAndDeleteOldUuid(loginUser.getToken(), loginUser.getUsername());
            
            return AjaxResult.success("success", jwtToken);
        } catch (Exception e) {
            log.error("系统内部出错", e);
            AjaxResult ajaxResult = AjaxResult.error("系统内部出错，请联系管理员." + e.getMessage());
            return ajaxResult;
        }

    }

    private Oauth2SsoUserInfo convertToUserInfo(Map<String, Object> userInfoMap) {
        Oauth2SsoUserInfo oauth2SsoUserInfo = new Oauth2SsoUserInfo();
        // displayName
        oauth2SsoUserInfo.setDisplayName(userInfoMap.get("displayName").toString());
        // loginName
        oauth2SsoUserInfo.setLoginName(userInfoMap.get("loginName").toString());
        // givenName
        oauth2SsoUserInfo.setGivenName(userInfoMap.get("givenName").toString());
        // mobile
        oauth2SsoUserInfo.setMobile(userInfoMap.get("mobile").toString());
        return oauth2SsoUserInfo;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> strToMap(String str) {
        scan: {
            if (StringUtils.isEmpty(str))
                break scan;
            ;
        }
        return JSON.parseObject(str, Map.class);
    }

}
