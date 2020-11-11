/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config;

import com.vmware.transport.bridge.spring.config.interceptors.AnyDestinationMatcher;
import com.vmware.transport.bridge.spring.config.interceptors.StartsWithDestinationMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.simp.stomp.StompCommand;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class TransportBridgeConfigurationTest {

    TransportBridgeConfiguration configuration;

    @Before
    public void before() {
        this.configuration = new TransportBridgeConfiguration();
    }

    @Test
    public void testAddTransportDestinationPrefixes() {
        this.configuration.addTransportDestinationPrefixes(
              "/topic", "/pub/channel", "/pub/channel2/", "/pub/channel2");

        Set<String> prefixes = this.configuration.getTransportDestinationPrefixes();
        Assert.assertEquals(prefixes.size(), 3);
        Assert.assertTrue(prefixes.containsAll(
              Arrays.asList("/topic/", "/pub/channel/", "/pub/channel2/")));
    }

    @Test
    public void testAddTransportStompInterceptor() {
        this.configuration.addTransportStompInterceptor(
              message -> message,
              EnumSet.of(StompCommand.SEND),
              new AnyDestinationMatcher(),
              100);

        this.configuration.addTransportStompInterceptor(
              message -> message,
              EnumSet.of(StompCommand.SEND),
              new StartsWithDestinationMatcher("/test"),
              20);

        List<StompInterceptorRegistration> interceptors =
              this.configuration.getRegisteredTransportStompInterceptors();
        Assert.assertEquals(interceptors.size(), 2);
        Assert.assertEquals(interceptors.get(0).priority, 20);
        Assert.assertEquals(interceptors.get(1).priority, 100);
    }
}
