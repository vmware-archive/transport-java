package com.vmware.transport.bus;

import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.model.MessageObjectHandlerConfig;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright(c) VMware Inc. 2017
 */
@SuppressWarnings("unchecked")
public class MessageHandlerImplTest {

    Consumer<Message> success, error;
    EventBus bus;
    MessageObjectHandlerConfig config;
    String testChannelSend = "#local-send";
    String testChannelReceive = "#local-return";
    Logger logger;
    int successCount = 0;
    int errorCount = 0;
    int closeCount = 0;
    Exception lastException;

    private Consumer<Message> generateSuccess() {
        return (Message message) -> {
            this.successCount++;
            Assert.assertEquals(String.class, message.getPayloadClass());
            Assert.assertEquals("meeseeks", message.getPayload());
            Assert.assertFalse(message.isError());
        };
    }

    private Consumer<Message> generateError() {
        return (Message message) -> {
            Assert.assertEquals(String.class, message.getPayloadClass());
            Assert.assertTrue(message.isError());
            Assert.assertEquals("this is heavy dude", message.getPayload());
            this.errorCount++;
        };
    }

    @Before
    public void setUp() throws Exception {
        this.success = this.generateSuccess();
        this.error = this.generateError();
        this.bus = new EventBusImpl();
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.successCount = 0;
        this.closeCount = 0;
        this.lastException = null;
    }

    @Test
    public void testHandleSingle() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(true);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        TestObserver<Message> observer = configureHandler(false);


        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");
        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks 2");
        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks 3");

        observer.assertValueCount(3);
        Assert.assertEquals(1, this.successCount);
    }

    @Test
    public void testHandleResponseStream() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);

        TestObserver<Message> observer = configureHandler(false);

        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");
        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");
        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");


        observer.assertValueCount(3);
        Assert.assertEquals(3, this.successCount);
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

        this.bus.sendRequestMessage(this.testChannelSend, "meeseeks");
        this.bus.sendRequestMessage(this.testChannelSend, "meeseeks");
        this.bus.sendRequestMessage(this.testChannelSend, "meeseeks");


        observer.assertValueCount(0);
        Assert.assertEquals(3, this.successCount);
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

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        this.bus.sendRequestMessage(this.testChannelSend, "show me the");

        observer.assertValueCount(0);
        Assert.assertEquals(0, this.successCount);

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

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer2 = this.bus.getApi().getRequestChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(null, null);


        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");
        this.bus.sendErrorMessage(this.testChannelSend, "meeseeks");

        handler.tick("chickie");
        handler.tick("fox");

        observer.assertValueCount(1);
        observer2.assertValueCount(2);

        Assert.assertEquals(0, this.successCount);
        Assert.assertEquals(0, this.errorCount);

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

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer2 = this.bus.getApi().getRequestChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer3 = this.bus.getApi().getErrorChannel(this.testChannelSend, this.getClass().getName()).test();

        observer3.assertSubscribed();
        observer3.assertValueCount(0);

        handler.error("ignore me, not subscribed yet");

        observer3.assertValueCount(0);

        Disposable sub = handler.handle(this.success);

        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");
        handler.tick("pot");
        handler.tick("kettle");
        handler.tick("black");

        observer.assertValueCount(1);
        observer2.assertValueCount(3);

        // two requests were made, only one was a response to the handler.
        Assert.assertEquals(1, this.successCount);
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
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus, aVoid -> {
            this.closeCount++;
        });

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        Disposable sub = handler.handle(null);

        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");

        observer.assertValueCount(1);

        Assert.assertEquals(0, this.successCount);
        Assert.assertFalse(sub.isDisposed());
        Assert.assertFalse(handler.isClosed());
        Assert.assertEquals(0, this.closeCount);
        handler.close();
        Assert.assertTrue(handler.isClosed());
        Assert.assertTrue(sub.isDisposed());
        Assert.assertEquals(1, this.closeCount);
        handler.close();
        Assert.assertEquals(1, this.closeCount);
    }

    @Test
    public void testTickNoHandler() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getApi().getRequestChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.tick("tickle");
        observer.assertValueCount(0);

        Assert.assertEquals(0, this.successCount);

    }

    @Test
    public void testStreamSuccessAndErrorHandlers() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(false);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus, aVoid -> {
            this.closeCount++;
            throw new RuntimeException("OnCloseHandler exception");
        });

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer2 = this.bus.getApi().getRequestChannel(this.testChannelSend, this.getClass().getName()).test();
        TestObserver<Message> observer3 = this.bus.getApi().getErrorChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(this.success, this.error);


        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");
        this.bus.sendErrorMessage(this.testChannelSend, "this is heavy dude");

        handler.tick("gobble");
        handler.tick("wobble");

        this.bus.sendErrorMessage(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);
        observer2.assertValueCount(2);
        observer3.assertValueCount(2);

        Assert.assertEquals(1, this.successCount);
        Assert.assertEquals(2, this.errorCount);

        try {
            handler.close();
        } catch (Exception ex) {
            lastException = ex;
        }
        Assert.assertTrue(handler.isClosed());
        Assert.assertEquals(1, this.closeCount);
        Assert.assertNull(lastException);
    }

    @Test
    public void testSingleSuccessHandler() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(true);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(this.success, this.error);

        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");
        this.bus.sendErrorMessage(this.testChannelSend, "this is heavy dude");

        handler.tick("meeseeks");
        handler.tick("meeseeks");

        this.bus.sendErrorMessage(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);

        Assert.assertEquals(1, this.successCount);
        Assert.assertEquals(0, this.errorCount);

        handler.close();
        Assert.assertTrue(handler.isClosed());
    }

    @Test
    public void testSingleErrorHandler() {

        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(true);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
        MessageHandler handler = new MessageHandlerImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        handler.handle(this.success, this.error);

        this.bus.sendErrorMessage(this.testChannelSend, "this is heavy dude");
        this.bus.sendResponseMessage(this.testChannelSend, "meeseeks");

        handler.tick("meeseeks");
        handler.tick("meeseeks");

        this.bus.sendErrorMessage(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);

        Assert.assertEquals(0, this.successCount);
        Assert.assertEquals(1, this.errorCount);

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

        TestObserver<Message> observer = this.bus.getApi().getErrorChannel(this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.handle(this.success, this.error);

        this.bus.sendErrorMessage(this.testChannelSend, "this is heavy dude");

        observer.assertValueCount(1);

        Assert.assertEquals(0, this.successCount);
        Assert.assertEquals(1, this.errorCount);
    }

    private TestObserver<Message> configureHandler(boolean requestStream) {
        MessageHandler handler = new MessageHandlerImpl(requestStream, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getApi().getResponseChannel(this.testChannelSend, this.getClass().getName()).test();

        observer.assertSubscribed();

        handler.handle(this.success);

        return observer;
    }

}