/**
 * Copyright(c) VMware Inc. 2017
 */
package com.vmware.transport.bus.model;

import org.junit.Assert;
import org.junit.Test;

public class MessageObjectTest {

    @Test
    public void createBasicMessage() {

        MessageObject<String> messageObject = new MessageObject<>(MessageType.MessageTypeRequest, "#test-me");
        Assert.assertEquals(String.class, messageObject.getPayloadClass());
        Assert.assertEquals("#test-me", messageObject.getPayload());

        MessageObject<Channel> messageObject2 = new MessageObject<>(MessageType.MessageTypeRequest, new Channel("#magic-shoes"));
        Assert.assertEquals(Channel.class, messageObject2.getPayloadClass());
        Assert.assertEquals("#magic-shoes", messageObject2.getPayload().getName());
    }

    @Test
    public void testMessageProperties() {
        MessageObject messageObject = new MessageObject(MessageType.MessageTypeRequest, "park life");
        Assert.assertEquals(String.class, messageObject.getPayloadClass());
        Assert.assertNotEquals(Integer.class, messageObject.getPayloadClass());
        Assert.assertEquals("park life", messageObject.toString());

        messageObject.setPayload(new Long(1234567));
        messageObject.setPayloadClass(Long.class);

        Assert.assertNotEquals(String.class, messageObject.getPayloadClass());
        Assert.assertEquals(Long.class, messageObject.getPayloadClass());
        Assert.assertEquals(MessageType.MessageTypeRequest, messageObject.getType());

        messageObject.setType(MessageType.MessageTypeResponse);

        Assert.assertEquals(MessageType.MessageTypeResponse, messageObject.getType());

    }

}