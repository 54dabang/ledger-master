package com.ledger.framework.security.oauth2sso;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
public class Oauth2ssoAuthenticationSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    @Autowired
    private UserDetailsService userDetailsService;
//    @Autowired
//    private Oauth2ssoAuthenticationSuccessHandler oauth2ssoAuthenticationSuccessHandler;
//    @Autowired
//    private Oauth2ssoAuthenticationFailureHandler oauth2ssoAuthenticationFailureHandler;

    @Override
    public void configure(HttpSecurity http) throws Exception {
        Oauth2ssoAuthenticationFilter smsCodeAuthenticationFilter = new Oauth2ssoAuthenticationFilter();
        smsCodeAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        
        //smsCodeAuthenticationFilter.setAuthenticationSuccessHandler(oauth2ssoAuthenticationSuccessHandler);
        //smsCodeAuthenticationFilter.setAuthenticationFailureHandler(oauth2ssoAuthenticationFailureHandler);

        Oauth2ssoAuthenticationProvider smsCodeAuthenticationProvider = new Oauth2ssoAuthenticationProvider();
        smsCodeAuthenticationProvider.setUserDetailsService(userDetailsService);

        http.authenticationProvider(smsCodeAuthenticationProvider)
                .addFilterAfter(smsCodeAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
