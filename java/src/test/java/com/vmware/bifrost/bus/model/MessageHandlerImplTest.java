package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright(c) VMware Inc. 2017
 */
@SuppressWarnings("unchecked")
public class MessageHandlerImplTest {

    Consumer<Message> success, error;
    MessagebusService bus;
    MessageObjectHandlerConfig config;
    String testChannelSend = "#local-send";
    String testChannelReceive = "#local-return";
    Logger logger;
    int successCount = 0;
    int errorCount = 0;

    private Consumer<Message> generateSuccess() {
        return (Message message) -> {
            this.successCount++;
            Assert.assertEquals(message.getPayloadClass(), String.class);
            Assert.assertEquals(message.getPayload(), "meeseeks");
            Assert.assertFalse(message.isError());
            Assert.assertTrue(message.isResponse());
        };
    }

    private Consumer<Message> generateError() {
        return (Message message) -> {
            Assert.assertEquals(message.getPayloadClass(), String.class);
            Assert.assertTrue(message.isError());
            Assert.assertEquals(message.getPayload(), "this is heavy dude");
            this.errorCount++;
        };
    }

    @Before
    public void setUp() throws Exception {
        this.success = this.generateSuccess();
        this.error = this.generateError();
        this.bus = new MessagebusService();
        logger = LoggerFactory.getLogger(this.getClass());
        this.successCount = 0;
    }

    @Test
    public void testHandleSingle() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(true);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        TestObserver<Message> observer = configureHandler();


        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendResponse(this.testChannelSend, "meeseeks 2");
        this.bus.sendResponse(this.testChannelSend, "meeseeks 3");

        observer.assertValueCount(3);
        Assert.assertEquals(this.successCount, 1);
    }

    @Test
    public void testHandleStream() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        TestObserver<Message> observer = configureHandler();

        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendResponse(this.testChannelSend, "meeseeks");


        observer.assertValueCount(3);
        Assert.assertEquals(this.successCount, 3);
        observer.dispose();
        Assert.assertTrue(observer.isDisposed());
    }

    @Test
    public void testHelpers() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(this.success);


        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        handler.tick("meeseeks");
        handler.tick("meeseeks");

        observer.assertValueCount(3);

        Assert.assertEquals(this.successCount, 3);

        handler.close();
        Assert.assertTrue(handler.isClosed());

    }

    @Test
    public void testErrorHandling() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.handle(this.success, this.error);

        this.bus.sendError(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);

        Assert.assertEquals(this.successCount, 0);
        Assert.assertEquals(this.errorCount, 1);
    }

    private TestObserver<Message> configureHandler() {
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        observer.assertSubscribed();

        handler.handle(this.success);

        return observer;
    }

}