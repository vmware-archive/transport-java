/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostChannelInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.powermock.reflect.Whitebox;

public class BifrostSpringConfigTest {

    private BifrostSpringConfig config;

    @Before
    public void before() {
        this.config = new BifrostSpringConfig();
    }

    @Test
    public void testBeansCreation() {
        Assert.assertNotNull(config.bifrostBridgeConfiguration());
        Assert.assertNotNull(config.parameterNameDiscoverer());
    }

    @Test
    public void testConfigureClientInboundChannel() {
        ChannelRegistration channelRegistration = Mockito.mock(ChannelRegistration.class);
        this.config.configureClientInboundChannel(channelRegistration);
        Mockito.verify(channelRegistration, Mockito.times(1)).interceptors(
              Mockito.any(BifrostChannelInterceptor.class));

    }

    @Test
    public void testBifrostBridgeConfigurers() {

        // Verify that no errors are thrown if there aren't any
        // bridge configurers
        this.config.afterSingletonsInstantiated();

        BifrostBridgeConfigurer bridgeConfigurerImpl = new BifrostBridgeConfigurer() {
            public void registerBifrostDestinationPrefixes(BifrostBridgeConfiguration configuration) {
                Assert.assertEquals(configuration, config.bifrostBridgeConfiguration());
            }
            public void registerBifrostStompInterceptors(BifrostBridgeConfiguration configuration) {
                Assert.assertEquals(configuration, config.bifrostBridgeConfiguration());
            }
        };
        BifrostBridgeConfigurer bridgeConfigurer1 = Mockito.spy(bridgeConfigurerImpl);
        BifrostBridgeConfigurer bridgeConfigurer2 = Mockito.spy(new BifrostBridgeConfigurer() {});

        Whitebox.setInternalState(this.config, "bifrostBridgeConfigurers",
              new BifrostBridgeConfigurer[] {bridgeConfigurer1, bridgeConfigurer2});

        this.config.afterSingletonsInstantiated();

        Mockito.verify(bridgeConfigurer1, Mockito.times(1)).registerBifrostStompInterceptors(
              this.config.bifrostBridgeConfiguration());
        Mockito.verify(bridgeConfigurer1, Mockito.times(1)).registerBifrostDestinationPrefixes(
              this.config.bifrostBridgeConfiguration());
        Mockito.verify(bridgeConfigurer2, Mockito.times(1)).registerBifrostStompInterceptors(
              this.config.bifrostBridgeConfiguration());
        Mockito.verify(bridgeConfigurer2, Mockito.times(1)).registerBifrostDestinationPrefixes(
              this.config.bifrostBridgeConfiguration());
    }
}
