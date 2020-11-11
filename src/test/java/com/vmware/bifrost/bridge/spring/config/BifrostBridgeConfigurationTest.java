/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import com.vmware.bifrost.bridge.spring.config.interceptors.AnyDestinationMatcher;
import com.vmware.bifrost.bridge.spring.config.interceptors.StartsWithDestinationMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.simp.stomp.StompCommand;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class BifrostBridgeConfigurationTest {

    BifrostBridgeConfiguration configuration;

    @Before
    public void before() {
        this.configuration = new BifrostBridgeConfiguration();
    }

    @Test
    public void testAddBifrostDestinationPrefixes() {
        this.configuration.addBifrostDestinationPrefixes(
              "/topic", "/pub/channel", "/pub/channel2/", "/pub/channel2");

        Set<String> prefixes = this.configuration.getBifrostDestinationPrefixes();
        Assert.assertEquals(prefixes.size(), 3);
        Assert.assertTrue(prefixes.containsAll(
              Arrays.asList("/topic/", "/pub/channel/", "/pub/channel2/")));
    }

    @Test
    public void testAddBifrostStompInterceptor() {
        this.configuration.addBifrostStompInterceptor(
              message -> message,
              EnumSet.of(StompCommand.SEND),
              new AnyDestinationMatcher(),
              100);

        this.configuration.addBifrostStompInterceptor(
              message -> message,
              EnumSet.of(StompCommand.SEND),
              new StartsWithDestinationMatcher("/test"),
              20);

        List<StompInterceptorRegistration> interceptors =
              this.configuration.getRegisteredBifrostStompInterceptors();
        Assert.assertEquals(interceptors.size(), 2);
        Assert.assertEquals(interceptors.get(0).priority, 20);
        Assert.assertEquals(interceptors.get(1).priority, 100);
    }
}
