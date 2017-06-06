package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.function.Supplier;

import static org.junit.Assert.*;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageHandlerImplTest {

    Consumer<Message> success, error;
    MessagebusService bus;
    MessageObjectHandlerConfig config;
    String testChannelSend = "#local-send";
    String testChannelRecieve = "#local-recieve";
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
            Assert.assertTrue(message.isResponse());
            Assert.assertEquals(message.getPayload(), "woah");
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
    }

    private TestObserver<Message> configureHandler() {
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        observer.assertSubscribed();

        handler.handle(this.success);


        return observer;
    }

}