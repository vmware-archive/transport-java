/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostChannelInterceptor;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Loads Bifrost message bus and bridge components.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.vmware.bifrost.bridge.spring.config",
        "com.vmware.bifrost.bridge.spring.controllers",
        "com.vmware.bifrost.bridge.spring.handlers",
        "com.vmware.bifrost.bridge.spring",
        "com.vmware.bifrost.bridge",
        "com.vmware.bifrost.bus",
        "com.vmware.bifrost.bus.store",
        "com.vmware.bifrost.core",
        "com.vmware.bifrost.core.operations"
})
public class BifrostSpringConfig extends AbstractWebSocketMessageBrokerConfigurer
      implements SmartInitializingSingleton {

    @Autowired(required = false)
    private BifrostBridgeConfigurer[] bifrostBridgeConfigurers;

    private final BifrostBridgeConfiguration bridgeConfiguration = new BifrostBridgeConfiguration();

    @Bean
    public BifrostBridgeConfiguration bifrostBridgeConfiguration() {
        return bridgeConfiguration;
    }

    @Bean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (bifrostBridgeConfigurers != null) {
            for (BifrostBridgeConfigurer configurer : bifrostBridgeConfigurers) {
                configurer.registerBifrostDestinationPrefixes(bifrostBridgeConfiguration());
                configurer.registerBifrostStompInterceptors(bifrostBridgeConfiguration());
                configurer.configureGalacticChannels();
            }
        }
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Do nothing. We are interested only in overriding the
        // configureClientInboundChannel method.
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        BifrostChannelInterceptor bifrostChannelInterceptor =
              new BifrostChannelInterceptor(this.bridgeConfiguration);

        registration.interceptors(bifrostChannelInterceptor);
    }
}
