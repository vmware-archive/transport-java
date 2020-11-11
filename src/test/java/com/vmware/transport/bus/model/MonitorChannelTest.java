package com.vmware.transport.bus.model;


import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MonitorChannelTest {

    @Test
    public void testModel() {
        Assert.assertNotNull(MonitorChannel.stream);
        MonitorChannel chan = new MonitorChannel();
        Assert.assertNotNull(chan);
    }
}