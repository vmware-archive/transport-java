/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BifrostUtilTest {

    private BifrostBridgeConfiguration bifrostBridgeConfiguration;

    @Before
    public void before() {
        this.bifrostBridgeConfiguration = new BifrostBridgeConfiguration();
        this.bifrostBridgeConfiguration.addBifrostDestinationPrefixes(
              "/bifrost-prefix1", "/bifrost-prefix2/");
    }

    @Test
    public void testGetBifrostDestinationPrefixWithNullDestination() {
        Assert.assertNull(BifrostUtil.getBifrostDestinationPrefix(
              this.bifrostBridgeConfiguration, null));
    }

    @Test
    public void testGetBifrostDestinationPrefixWithNonBifrostDestination() {
        Assert.assertNull(BifrostUtil.getBifrostDestinationPrefix(
              this.bifrostBridgeConfiguration, ""));
        Assert.assertNull(BifrostUtil.getBifrostDestinationPrefix(
              this.bifrostBridgeConfiguration, "/topic/channel1"));
    }

    @Test
    public void testGetBifrostDestinationPrefixWithBifrostDestination() {
        Assert.assertEquals(BifrostUtil.getBifrostDestinationPrefix(
              this.bifrostBridgeConfiguration, "/bifrost-prefix2/channel1"), "/bifrost-prefix2/");
    }

    @Test
    public void testExtractChannelNameWithNonBifrostDestination() {
        Assert.assertNull(BifrostUtil.extractChannelName(
              this.bifrostBridgeConfiguration, null));
        Assert.assertNull(BifrostUtil.extractChannelName(
              this.bifrostBridgeConfiguration, ""));
        Assert.assertNull(BifrostUtil.extractChannelName(
              this.bifrostBridgeConfiguration, "/topic/channel1"));
    }

    @Test
    public void testExtractChannelNameWithBifrostDestination() {
        Assert.assertEquals(BifrostUtil.extractChannelName(
              this.bifrostBridgeConfiguration, "/bifrost-prefix2/channel1"), "channel1");
    }
}
