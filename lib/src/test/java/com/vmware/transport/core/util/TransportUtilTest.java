/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.core.util;

import com.vmware.transport.bridge.spring.config.TransportBridgeConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TransportUtilTest {

    private TransportBridgeConfiguration transportBridgeConfiguration;

    @Before
    public void before() {
        this.transportBridgeConfiguration = new TransportBridgeConfiguration();
        this.transportBridgeConfiguration.addTransportDestinationPrefixes(
              "/transport-prefix1", "/transport-prefix2/");
    }

    @Test
    public void testGetTransportDestinationPrefixWithNullDestination() {
        Assert.assertNull(TransportUtil.getTransportDestinationPrefix(
              this.transportBridgeConfiguration, null));
    }

    @Test
    public void testGetTransportDestinationPrefixWithNonTransportDestination() {
        Assert.assertNull(TransportUtil.getTransportDestinationPrefix(
              this.transportBridgeConfiguration, ""));
        Assert.assertNull(TransportUtil.getTransportDestinationPrefix(
              this.transportBridgeConfiguration, "/topic/channel1"));
    }

    @Test
    public void testGetTransportDestinationPrefixWithTransportDestination() {
        Assert.assertEquals(TransportUtil.getTransportDestinationPrefix(
              this.transportBridgeConfiguration, "/transport-prefix2/channel1"), "/transport-prefix2/");
    }

    @Test
    public void testExtractChannelNameWithNonTransportDestination() {
        Assert.assertNull(TransportUtil.extractChannelName(
              this.transportBridgeConfiguration, null));
        Assert.assertNull(TransportUtil.extractChannelName(
              this.transportBridgeConfiguration, ""));
        Assert.assertNull(TransportUtil.extractChannelName(
              this.transportBridgeConfiguration, "/topic/channel1"));
    }

    @Test
    public void testExtractChannelNameWithTransportDestination() {
        Assert.assertEquals(TransportUtil.extractChannelName(
              this.transportBridgeConfiguration, "/transport-prefix2/channel1"), "channel1");
    }
}
