package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.MessageResponder;
import com.vmware.bifrost.bus.MessageResponderImpl;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

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
    int responseCount = 0;

    Function generate;

    private Function<Message, String> generateResponse() {
        return (Message message) -> {
            Assert.assertEquals(String.class, message.getPayloadClass());
            Assert.assertEquals("show me the",message.getPayload());
            Assert.assertFalse(message.isError());
            Assert.assertTrue(message.isRequest());
            this.responseCount++;
            return "money";
        };
    }

    private Function<Message, String> generateErrorResponse() {
        return (Message message) -> {
            Assert.assertEquals("show me the", message.getPayload());
            Assert.assertTrue(message.isError());
            Assert.assertTrue(message.isResponse());
            this.responseCount++;
            return "way to go home";
        };
    }


    @Before
    public void setUp() throws Exception {
        this.generate = this.generateResponse();
        this.bus = new MessagebusService();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Test
    public void testCloseBeforeRespond() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        this.bus.sendRequest(this.testChannelSend, "show me the");

        observer.assertValueCount(0);
        Assert.assertEquals(0, this.responseCount);

        Assert.assertFalse(handler.isClosed());
        handler.close();
        Assert.assertFalse(handler.isClosed());

    }

    @Test
    public void testSingleResponder() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.generate(this.generateResponse());

        this.bus.sendRequest(this.testChannelSend, "show me the");

        observer.assertValueCount(1);
        Assert.assertEquals(1, this.responseCount);

        for (Message msg : observer.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertEquals(msg.getPayload(), "money");
        }

    }

    @Test
    public void testStreamResponderTickClose() throws Exception {

        setConfig(false);

        MessageResponder<String> handler = new MessageResponderImpl(true, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        Disposable sub = handler.generate(this.generateResponse());

        this.bus.sendRequest(this.testChannelSend, "show me the");
        handler.tick("money");
        handler.tick("money");
        handler.tick("money");

        observer.assertValueCount(4);
        Assert.assertEquals(1, this.responseCount);

        for (Message msg : observer.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertEquals(msg.getPayload(), "money");
        }

        Assert.assertFalse(sub.isDisposed());
        Assert.assertFalse(handler.isClosed());
        handler.close();
        Assert.assertTrue(sub.isDisposed());
        Assert.assertTrue(handler.isClosed());
        handler.close();
        Assert.assertTrue(handler.isClosed());

    }


    @Test
    public void testSingleNullResponder() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.generate(null);

        this.bus.sendRequest(this.testChannelSend, "show me the");

        observer.assertValueCount(0);
        Assert.assertEquals(0, this.responseCount);


    }

    @Test
    public void testSingleErrorNullResponder() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.generate(null);

        this.bus.sendError(this.testChannelSend, "show me the");

        observer.assertValueCount(0);
        Assert.assertEquals(0, this.responseCount);

    }

    @Test
    public void testSingleErrorResponder() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();

        TestObserver<Message> observer2 = this.bus.getErrorChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.generate(this.generateErrorResponse());

        this.bus.sendError(this.testChannelSend, "show me the");

        observer.assertValueCount(0);
        observer2.assertValueCount(1);
        Assert.assertEquals(0, this.responseCount);

        for (Message msg : observer2.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertTrue(msg.isError());
            Assert.assertEquals("show me the", msg.getPayload());
        }
    }

    @Test
    public void testSingleResponderTickClose() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        Disposable sub = handler.generate(this.generateResponse());

        this.bus.sendRequest(this.testChannelSend, "show me the");
        handler.tick("show me the");
        handler.tick("show me the");

        handler.error("oh dear");
        handler.error("oh dear");

        observer.assertValueCount(1);

        Assert.assertEquals(this.responseCount, 1);

        for (Message msg : observer.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertFalse(msg.isError());
            Assert.assertEquals("money", msg.getPayload());
        }
        Assert.assertTrue(sub.isDisposed());
    }

    @Test
    public void testSingleResponderTickErrorNull() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();

        TestObserver<Message> observer2 = this.bus.getErrorChannel(
                this.testChannelSend, this.getClass().getName()).test();

        observer.assertSubscribed();
        observer2.assertSubscribed();

        handler.error("oh dear");
        handler.error("oh dear");

        this.bus.sendRequest(this.testChannelSend, "show me the");
        handler.tick("show me the");
        handler.tick("show me the");

        observer.assertValueCount(0);
        observer2.assertValueCount(0);

        Assert.assertEquals(this.responseCount, 0);

    }


    private void setConfig(boolean singleResponse) {
        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(singleResponse);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
    }

    @Test
    public void testSingleResponderTickErrorNoSub() throws Exception {

        setConfig(true);

        MessageResponder<String> responder = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();

        TestObserver<Message> observer2 = this.bus.getErrorChannel(
                this.testChannelSend, this.getClass().getName()).test();

        observer.assertSubscribed();
        observer2.assertSubscribed();

        Disposable sub = responder.generate(this.generateResponse());

        this.bus.sendRequest(this.testChannelSend, "show me the");
        responder.tick("show me the");
        responder.tick("show me the");

        observer.assertValueCount(1);

        Assert.assertEquals(this.responseCount, 1);

        for (Message msg : observer.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertFalse(msg.isError());
            Assert.assertEquals("money", msg.getPayload());
        }
        Assert.assertTrue(sub.isDisposed());
    }

}