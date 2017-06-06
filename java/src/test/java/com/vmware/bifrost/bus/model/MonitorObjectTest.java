package com.vmware.bifrost.bus.model;


import org.junit.Assert;
import org.junit.Test;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MonitorObjectTest {

    @Test
    public void testModel() {
        MonitorObject obj = new MonitorObject(MonitorType.MonitorNewChannel, "#chan","me","hello");
        Assert.assertEquals(obj.getChannel(), "#chan");
        Assert.assertEquals(obj.getFrom(), "me");
        Assert.assertEquals(obj.getData(), "hello");
        Assert.assertEquals(obj.getType(), MonitorType.MonitorNewChannel);
        Assert.assertTrue(obj.isNewChannel());
        Assert.assertTrue(obj.hasData());

        obj.setType(MonitorType.MonitorCloseChannel);

        Assert.assertEquals(obj.getType(), MonitorType.MonitorCloseChannel);
        Assert.assertFalse(obj.isNewChannel());

        obj.setData(null);

        Assert.assertFalse(obj.hasData());

        obj.setChannel("#another");
        Assert.assertEquals(obj.getChannel(), "#another");

        obj.setFrom("the-other-guy");
        Assert.assertEquals(obj.getFrom(), "the-other-guy");

    }

}