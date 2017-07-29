package com.vmware.bifrost.bus.model;


import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MonitorObjectTest {

    @Test
    public void testModel() {
        MonitorObject obj = new MonitorObject(MonitorType.MonitorNewChannel, "#chan","me", "samples");
        Assert.assertEquals("#chan", obj.getChannel());
        Assert.assertEquals("me", obj.getFrom());
        Assert.assertEquals("samples", obj.getData());
        Assert.assertEquals(MonitorType.MonitorNewChannel, obj.getType());
        Assert.assertTrue(obj.isNewChannel());
        Assert.assertTrue(obj.hasData());

        obj.setType(MonitorType.MonitorCloseChannel);

        Assert.assertEquals(obj.getType(), MonitorType.MonitorCloseChannel);
        Assert.assertFalse(obj.isNewChannel());

        obj.setData(null);

        Assert.assertFalse(obj.hasData());

        obj.setChannel("#another");
        Assert.assertEquals("#another", obj.getChannel());

        obj.setFrom("the-other-guy");
        Assert.assertEquals("the-other-guy", obj.getFrom());

    }

}