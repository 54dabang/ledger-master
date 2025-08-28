package com.ledger.framework.security.oauth2sso;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 短信登陆鉴权 Provider，要求实现 AuthenticationProvider 接口
 * @author jitwxs
 * @since 2019/1/9 13:59
 */
public class Oauth2ssoAuthenticationProvider implements AuthenticationProvider {
    private UserDetailsService userDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Oauth2ssoAuthenticationToken authenticationToken = (Oauth2ssoAuthenticationToken) authentication;

        String username = (String) authenticationToken.getPrincipal();

        // TODO 检查从门户网站获取到的用户信息
        //checkSmsCode(mobile);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // 此时鉴权成功后，应当重新 new 一个拥有鉴权的 authenticationResult 返回
        Oauth2ssoAuthenticationToken authenticationResult = new Oauth2ssoAuthenticationToken(userDetails, userDetails.getAuthorities());
        authenticationResult.setDetails(authenticationToken.getDetails());
        return authenticationResult;
    }

//    private void checkSmsCode(String mobile) {
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        String inputCode = request.getParameter("smsCode");
//
//        Map<String, Object> smsCode = (Map<String, Object>) request.getSession().getAttribute("smsCode");
//        if(smsCode == null) {
//            throw new BadCredentialsException("未检测到申请验证码");
//        }
//
//        String applyMobile = (String) smsCode.get("mobile");
//        int code = (int) smsCode.get("code");
//
//        if(!applyMobile.equals(mobile)) {
//            throw new BadCredentialsException("申请的手机号码与登录手机号码不一致");
//        }
//        if(code != Integer.parseInt(inputCode)) {
//            throw new BadCredentialsException("验证码错误");
//        }
//    }

    @Override
    public boolean supports(Class<?> authentication) {
        // 判断 authentication 是不是 SmsCodeAuthenticationToken 的子类或子接口
        return Oauth2ssoAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
}
