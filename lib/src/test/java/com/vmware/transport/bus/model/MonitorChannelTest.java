/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MonitorChannelTest {

    @Test
    public void testModel() {
        Assert.assertNotNull(MonitorChannel.stream);
        MonitorChannel chan = new MonitorChannel();
        Assert.assertNotNull(chan);
    }
}