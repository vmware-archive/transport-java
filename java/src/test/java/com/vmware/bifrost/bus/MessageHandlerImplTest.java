package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.MessageHandler;
import com.vmware.bifrost.bus.MessageHandlerImpl;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import io.reactivex.disposables.Disposable;
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
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.successCount = 0;
    }

    @Test
    public void testHandleSingle() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(true);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        TestObserver<Message> observer = configureHandler(false);


        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendResponse(this.testChannelSend, "meeseeks 2");
        this.bus.sendResponse(this.testChannelSend, "meeseeks 3");

        observer.assertValueCount(3);
        Assert.assertEquals(this.successCount, 1);
    }

    @Test
    public void testHandleResponseStream() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        TestObserver<Message> observer = configureHandler(false);

        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendResponse(this.testChannelSend, "meeseeks");


        observer.assertValueCount(3);
        Assert.assertEquals(this.successCount, 3);
        observer.dispose();
        Assert.assertTrue(observer.isDisposed());
    }

    @Test
    public void testHandleStream() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        TestObserver<Message> observer = configureHandler(true);

        this.bus.sendRequest(this.testChannelSend, "meeseeks");
        this.bus.sendRequest(this.testChannelSend, "meeseeks");
        this.bus.sendRequest(this.testChannelSend, "meeseeks");


        observer.assertValueCount(0);
        Assert.assertEquals(this.successCount, 3);
        observer.dispose();
        Assert.assertTrue(observer.isDisposed());
    }

    @Test
    public void testNoHandlerClosing() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        this.bus.sendRequest(this.testChannelSend, "show me the");

        observer.assertValueCount(0);
        Assert.assertEquals(this.successCount, 0);

        Assert.assertFalse(handler.isClosed());
        handler.close();
        Assert.assertFalse(handler.isClosed());

    }


    @Test
    public void testNullHandlers() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer2 = this.bus.getRequestChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(null, null);


        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendError(this.testChannelSend, "meeseeks");

        handler.tick("chickie");
        handler.tick("fox");

        observer.assertValueCount(1);
        observer2.assertValueCount(2);

        Assert.assertEquals(this.successCount, 0);
        Assert.assertEquals(this.errorCount, 0);

        handler.close();
        Assert.assertTrue(handler.isClosed());

    }

    @Test
    public void testHelpers() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer2 = this.bus.getRequestChannel(this.testChannelSend, this.getClass().getName()).test();

        Disposable sub = handler.handle(this.success);

        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        handler.tick("pot");
        handler.tick("kettle");
        handler.tick("black");

        observer.assertValueCount(1);
        observer2.assertValueCount(3);

        // two requests were made, only one was a response to the handler.
        Assert.assertEquals(this.successCount, 1);
        Assert.assertFalse(sub.isDisposed());
        Assert.assertFalse(handler.isClosed());
        handler.close();
        Assert.assertTrue(handler.isClosed());
        Assert.assertTrue(sub.isDisposed());

    }

    @Test
    public void testHelpersCloseCheck() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        Disposable sub = handler.handle(null);

        this.bus.sendResponse(this.testChannelSend, "meeseeks");

        observer.assertValueCount(1);

        Assert.assertEquals(this.successCount, 0);
        Assert.assertFalse(sub.isDisposed());
        Assert.assertFalse(handler.isClosed());
        handler.close();
        Assert.assertTrue(handler.isClosed());
        Assert.assertTrue(sub.isDisposed());

    }

    @Test
    public void testStreamSuccessAndErrorHandlers() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer2 = this.bus.getRequestChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer3 = this.bus.getErrorChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(this.success, this.error);


        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendError(this.testChannelSend, "this is heavy dude");

        handler.tick("gobble");
        handler.tick("wobble");

        this.bus.sendError(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);
        observer2.assertValueCount(2);
        observer3.assertValueCount(2);

        Assert.assertEquals(this.successCount, 1);
        Assert.assertEquals(this.errorCount, 2);

        handler.close();
        Assert.assertTrue(handler.isClosed());

    }

    @Test
    public void testSingleSuccessAndErrorHandlers() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(true);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(this.success, this.error);

        this.bus.sendResponse(this.testChannelSend, "meeseeks");
        this.bus.sendError(this.testChannelSend, "this is heavy dude");

        handler.tick("meeseeks");
        handler.tick("meeseeks");

        this.bus.sendError(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);

        Assert.assertEquals(this.successCount, 1);
        Assert.assertEquals(this.errorCount, 1);

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

        TestObserver<Message> observer = this.bus.getErrorChannel(this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.handle(this.success, this.error);

        this.bus.sendError(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);

        Assert.assertEquals(this.successCount, 0);
        Assert.assertEquals(this.errorCount, 1);
    }

    private TestObserver<Message> configureHandler(boolean requestStream) {
        MessageHandler handler = new MessageHandlerImpl(requestStream, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        observer.assertSubscribed();

        handler.handle(this.success);

        return observer;
    }

}