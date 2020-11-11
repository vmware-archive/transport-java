/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config;

import com.vmware.transport.bridge.spring.config.interceptors.TransportChannelInterceptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.powermock.reflect.Whitebox;

public class TransportSpringConfigTest {

    private TransportSpringConfig config;

    @Before
    public void before() {
        this.config = new TransportSpringConfig();
    }

    @Test
    public void testBeansCreation() {
        Assert.assertNotNull(config.transportBridgeConfiguration());
        Assert.assertNotNull(config.parameterNameDiscoverer());
    }

    @Test
    public void testConfigureClientInboundChannel() {
        ChannelRegistration channelRegistration = Mockito.mock(ChannelRegistration.class);
        this.config.configureClientInboundChannel(channelRegistration);
        Mockito.verify(channelRegistration, Mockito.times(1)).interceptors(
              Mockito.any(TransportChannelInterceptor.class));

    }

    @Test
    public void testTransportBridgeConfigurers() {

        // Verify that no errors are thrown if there aren't any
        // bridge configurers
        this.config.afterSingletonsInstantiated();

        TransportBridgeConfigurer bridgeConfigurerImpl = new TransportBridgeConfigurer() {
            public void registerTransportDestinationPrefixes(TransportBridgeConfiguration configuration) {
                Assert.assertEquals(configuration, config.transportBridgeConfiguration());
            }
            public void registerTransportStompInterceptors(TransportBridgeConfiguration configuration) {
                Assert.assertEquals(configuration, config.transportBridgeConfiguration());
            }
        };
        TransportBridgeConfigurer bridgeConfigurer1 = Mockito.spy(bridgeConfigurerImpl);
        TransportBridgeConfigurer bridgeConfigurer2 = Mockito.spy(new TransportBridgeConfigurer() {});

        Whitebox.setInternalState(this.config, "transportBridgeConfigurers",
              new TransportBridgeConfigurer[] {bridgeConfigurer1, bridgeConfigurer2});

        this.config.afterSingletonsInstantiated();

        Mockito.verify(bridgeConfigurer1, Mockito.times(1)).registerTransportStompInterceptors(
              this.config.transportBridgeConfiguration());
        Mockito.verify(bridgeConfigurer1, Mockito.times(1)).registerTransportDestinationPrefixes(
              this.config.transportBridgeConfiguration());
        Mockito.verify(bridgeConfigurer2, Mockito.times(1)).registerTransportStompInterceptors(
              this.config.transportBridgeConfiguration());
        Mockito.verify(bridgeConfigurer2, Mockito.times(1)).registerTransportDestinationPrefixes(
              this.config.transportBridgeConfiguration());
    }
}
