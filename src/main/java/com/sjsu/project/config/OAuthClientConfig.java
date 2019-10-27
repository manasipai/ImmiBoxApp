package com.sjsu.project.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;


/**
 * this class is required only if you want to override default OAuth 2 client integration provided by
 * Spring Boot
 */
@Configuration
public class OAuthClientConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        .requiresChannel().anyRequest().requiresSecure()
        .and()
        	.authorizeRequests()
                .antMatchers( "/" ,"/login/**").permitAll()
             .anyRequest().
             	authenticated()
                .and()
                .oauth2Login();
    }
}
