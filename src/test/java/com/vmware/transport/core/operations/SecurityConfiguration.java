/*
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.transport.core.operations;


import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
//import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {


    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password("test123").roles("ADMIN");
        auth.inMemoryAuthentication().withUser("vmware").password("test123").roles("USER");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/secured/**").hasRole("USER")
                .antMatchers("/secured-admin/**").hasRole("ADMIN")
                .and().csrf().disable();
    }

//    @Bean
//    ServletWebServerFactory servletWebServerFactory(){
//        return new TomcatServletWebServerFactory();
//    }
}
