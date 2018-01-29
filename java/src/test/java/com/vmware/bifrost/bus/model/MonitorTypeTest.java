package com.vmware.bifrost.bus.model;

import org.junit.Assert;
import org.junit.Test;


/**
 * Copyright(c) VMware Inc. 2017
 */
public class MonitorTypeTest {

    @Test
    public void testModel() {
        Assert.assertEquals(7, MonitorType.values().length);
        Assert.assertEquals(MonitorType.valueOf("MonitorCloseChannel"), MonitorType.MonitorCloseChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorCompleteChannel"), MonitorType.MonitorCompleteChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorDestroyChannel"), MonitorType.MonitorDestroyChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorNewChannel"), MonitorType.MonitorNewChannel);
        Assert.assertEquals(MonitorType.valueOf("MonitorData"), MonitorType.MonitorData);
        Assert.assertEquals(MonitorType.valueOf("MonitorError"), MonitorType.MonitorError);
        Assert.assertEquals(MonitorType.valueOf("MonitorDropped"), MonitorType.MonitorDropped);

    }

}