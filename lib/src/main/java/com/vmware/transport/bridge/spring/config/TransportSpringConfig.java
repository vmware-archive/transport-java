/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.config;

import com.vmware.transport.bridge.spring.config.interceptors.TransportChannelInterceptor;
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
 * Loads Transport message bus and bridge components.
 */
@Configuration
@ComponentScan(basePackages = {
        "com.vmware.transport.bridge.spring.config",
        "com.vmware.transport.bridge.spring.controllers",
        "com.vmware.transport.bridge.spring.handlers",
        "com.vmware.transport.bridge.spring",
        "com.vmware.transport.bridge",
        "com.vmware.transport.bus",
        "com.vmware.transport.bus.store",
        "com.vmware.transport.core",
        "com.vmware.transport.core.operations"
})
public class TransportSpringConfig extends AbstractWebSocketMessageBrokerConfigurer
      implements SmartInitializingSingleton {

    @Autowired(required = false)
    private TransportBridgeConfigurer[] transportBridgeConfigurers;

    private final TransportBridgeConfiguration bridgeConfiguration = new TransportBridgeConfiguration();

    @Bean
    public TransportBridgeConfiguration transportBridgeConfiguration() {
        return bridgeConfiguration;
    }

    @Bean
    public ParameterNameDiscoverer parameterNameDiscoverer() {
        return new DefaultParameterNameDiscoverer();
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (transportBridgeConfigurers != null) {
            for (TransportBridgeConfigurer configurer : transportBridgeConfigurers) {
                configurer.registerTransportDestinationPrefixes(transportBridgeConfiguration());
                configurer.registerTransportStompInterceptors(transportBridgeConfiguration());
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
        TransportChannelInterceptor transportChannelInterceptor =
              new TransportChannelInterceptor(this.bridgeConfiguration);

        registration.interceptors(transportChannelInterceptor);
    }
}
