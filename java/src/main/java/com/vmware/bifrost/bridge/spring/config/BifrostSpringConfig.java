/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

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
        "com.vmware.bifrost.core",
        "com.vmware.bifrost.core.operations"
})
public class BifrostSpringConfig implements SmartInitializingSingleton {

    @Autowired(required = false)
    private BifrostBridgeConfigurer[] bifrostBridgeConfigurers;

    private final BifrostBridgeConfiguration bridgeConfiguration = new BifrostBridgeConfiguration();

    @Bean
    public BifrostBridgeConfiguration bifrostBridgeConfiguration() {
        return bridgeConfiguration;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (bifrostBridgeConfigurers != null) {
            for (BifrostBridgeConfigurer configurer : bifrostBridgeConfigurers) {
                configurer.registerBifrostDestinationPrefixes(bifrostBridgeConfiguration());
            }
        }
    }
}
