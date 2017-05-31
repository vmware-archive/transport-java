package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.Channel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageObjectTest {


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void createBasicMessage (){

        try {

            MessageObject<String> messageObject = new MessageObject<String>(MessageType.MessageTypeRequest, "#test-me");
            Assert.assertEquals(messageObject.getPayloadClass(), String.class);
            Assert.assertEquals( ((String) messageObject.getPayload()), "#test-me");

            MessageObject<Channel> messageObject2 = new MessageObject<Channel>(MessageType.MessageTypeRequest, new Channel("#magic-shoes"));
            Assert.assertEquals(messageObject2.getPayloadClass(), Channel.class);
            Assert.assertEquals( ((Channel) messageObject2.getPayload()).getName(), "#magic-shoes");



        } catch (Exception exp){
            System.out.println(exp.getMessage());
        }
        //Assert.assertEquals(message.getPayloadClass(), String.class);


    }

}