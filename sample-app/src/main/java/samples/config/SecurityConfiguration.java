/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    // No security features currently implemented for this sample app.
    // Note, overiding this method as a no-op is necessary to disable the standard
    // security features that are included when spring security is on the classpath,
    // such as form login.
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // disable CSRF for demo app, we want this wide open for everyone.
        http.csrf().disable();
    }
}
