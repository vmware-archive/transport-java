package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.Channel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageTest {


    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void createBasicMessage (){

        try {

            Message<String> message = new Message<String>(MessageType.MessageTypeRequest, "#test-me");
            Assert.assertEquals(message.getPayloadClass(), String.class);
            Assert.assertEquals( ((String) message.getPayload()), "#test-me");

            Message<Channel> message2 = new Message<Channel>(MessageType.MessageTypeRequest, new Channel("#magic-shoes"));
            Assert.assertEquals(message2.getPayloadClass(), Channel.class);
            Assert.assertEquals( ((Channel) message2.getPayload()).getName(), "#magic-shoes");



        } catch (Exception exp){
            System.out.println(exp.getMessage());
        }
        //Assert.assertEquals(message.getPayloadClass(), String.class);


    }

}