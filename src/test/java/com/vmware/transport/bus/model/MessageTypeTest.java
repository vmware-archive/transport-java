package com.vmware.transport.bus.model;

import org.junit.Assert;
import org.junit.Test;


/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageTypeTest {

    @Test
    public void testModel() {
        Assert.assertEquals(3, MessageType.values().length);
        Assert.assertEquals(MessageType.valueOf("MessageTypeError"), MessageType.MessageTypeError);
        Assert.assertEquals(MessageType.valueOf("MessageTypeResponse"), MessageType.MessageTypeResponse);
        Assert.assertEquals(MessageType.valueOf("MessageTypeRequest"), MessageType.MessageTypeRequest);

    }

}