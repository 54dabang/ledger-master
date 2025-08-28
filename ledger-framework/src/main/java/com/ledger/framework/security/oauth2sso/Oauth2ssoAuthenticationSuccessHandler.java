package com.ledger.framework.security.oauth2sso;//package com.ruoyi.framework.security.oauth2sso;
//
//import java.io.IOException;
//
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.stereotype.Component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//
//import lombok.extern.slf4j.Slf4j;
//
//@Component
//@Slf4j
//public class Oauth2ssoAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
//    @Autowired
//    private ObjectMapper objectMapper;
//
//
//
//    @Override
//    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
//        log.info("oauth2sso登录成功");
//
//        //response.setContentType("application/json;charset=UTF-8");
//        //response.getWriter().write(objectMapper.writeValueAsString(authentication));
//    }
//}
