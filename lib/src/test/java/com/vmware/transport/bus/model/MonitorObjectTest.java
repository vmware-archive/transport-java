/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import org.junit.Assert;
import org.junit.Test;

public class MonitorObjectTest {

    @Test
    public void testModel() {
        MonitorObject obj = new MonitorObject(MonitorType.MonitorNewChannel, "#chan","me", "oldsamples");
        Assert.assertEquals("#chan", obj.getChannel());
        Assert.assertEquals("me", obj.getFrom());
        Assert.assertEquals("oldsamples", obj.getData());
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