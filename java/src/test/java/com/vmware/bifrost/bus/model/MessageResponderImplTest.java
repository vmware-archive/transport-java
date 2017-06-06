package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Copyright(c) VMware Inc. 2017
 */
@SuppressWarnings("unchecked")
public class MessageResponderImplTest {

    MessagebusService bus;
    MessageObjectHandlerConfig config;
    String testChannelSend = "#local-send";
    String testChannelReceive = "#local-return";
    Logger logger;
    int successCount = 0;

    Function generate;

    private Function<Message, String> generateResponse() {
        return (Message message) -> {
            Assert.assertEquals(message.getPayloadClass(), String.class);
            Assert.assertEquals(message.getPayload(), "show me the");
            Assert.assertFalse(message.isError());
            Assert.assertTrue(message.isRequest());
            return "money";
        };
    }


    @Before
    public void setUp() throws Exception {
        this.generate = this.generateResponse();
        this.bus = new MessagebusService();
    }

    @Test
    public void testSingleResponder() throws Exception {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(true);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.generate(this.generateResponse());

        this.bus.sendRequest(this.testChannelSend, "show me the");

        observer.assertValueCount(1);
        for (Message msg : observer.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertEquals(msg.getPayload(), "money");
        }

    }

}