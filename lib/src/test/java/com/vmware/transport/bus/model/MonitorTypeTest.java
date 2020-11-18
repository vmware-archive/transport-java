/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import org.junit.Assert;
import org.junit.Test;

public class MonitorTypeTest {

    @Test
    public void testModel() {
        Assert.assertEquals(9, MonitorType.values().length);
        Assert.assertEquals(MonitorType.valueOf("MonitorCloseChannel"), MonitorType.MonitorCloseChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorCompleteChannel"), MonitorType.MonitorCompleteChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorDestroyChannel"), MonitorType.MonitorDestroyChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorNewChannel"), MonitorType.MonitorNewChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorData"), MonitorType.MonitorData);
        Assert.assertEquals(MonitorType.valueOf("MonitorError"), MonitorType.MonitorError);
        Assert.assertEquals(MonitorType.valueOf("MonitorDropped"), MonitorType.MonitorDropped);
        Assert.assertEquals(MonitorType.valueOf("MonitorNewBridgeSubscription"), MonitorType.MonitorNewBridgeSubscription);
        Assert.assertEquals(MonitorType.valueOf("MonitorNewGalacticChannel"), MonitorType.MonitorNewGalacticChannel);

    }

}