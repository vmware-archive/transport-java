package com.vmware.bifrost.bus.model;

import org.junit.Assert;
import org.junit.Test;


/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageTypeTest {

    @Test
    public void testModel() {
        Assert.assertEquals(MessageType.values().length, 3);
        Assert.assertEquals(MessageType.valueOf("MessageTypeError"), MessageType.MessageTypeError);
        Assert.assertEquals(MessageType.valueOf("MessageTypeResponse"), MessageType.MessageTypeResponse);
        Assert.assertEquals(MessageType.valueOf("MessageTypeRequest"), MessageType.MessageTypeRequest);

    }

}