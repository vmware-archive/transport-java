package com.vmware.bifrost.bus.model;

import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.disposables.Disposable;
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
    int responseCount = 0;

    Function generate;

    private Function<Message, String> generateResponse() {
        return (Message message) -> {
            Assert.assertEquals(message.getPayloadClass(), String.class);
            Assert.assertEquals(message.getPayload(), "show me the");
            Assert.assertFalse(message.isError());
            Assert.assertTrue(message.isRequest());
            this.responseCount++;
            return "money";
        };
    }

    private Function<Message, String> generateErrorResponse() {
        return (Message message) -> {
            Assert.assertEquals(message.getPayloadClass(), String.class);
            Assert.assertEquals(message.getPayload(), "show me the");
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
        Assert.assertEquals(this.responseCount, 0);

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
        Assert.assertEquals(this.responseCount, 1);

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
        Assert.assertEquals(this.responseCount, 1);

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
        Assert.assertEquals(this.responseCount, 0);


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

        observer.assertValueCount(1);
        Assert.assertEquals(this.responseCount, 0);

    }

    @Test
    public void testSingleErrorResponder() throws Exception {

        setConfig(true);

        MessageResponder<String> handler = new MessageResponderImpl(false, this.config, this.bus);

        TestObserver<Message> observer = this.bus.getResponseChannel(
                this.testChannelSend, this.getClass().getName()).test();
        observer.assertSubscribed();

        handler.generate(this.generateErrorResponse());

        this.bus.sendError(this.testChannelSend, "show me the");

        observer.assertValueCount(2);
        Assert.assertEquals(this.responseCount, 1);

        int x = 0;
        for (Message msg : observer.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertTrue(msg.isError());
            if(x == 0) {
                Assert.assertEquals(msg.getPayload(), "show me the");
                x++;
            }
            if(x == 0) {
                Assert.assertEquals(msg.getPayload(), "way to go home");
                x++;
            }
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

        observer.assertValueCount(1);

        Assert.assertEquals(this.responseCount, 1);

        for (Message msg : observer.values()) {
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertFalse(msg.isError());
            Assert.assertEquals(msg.getPayload(), "money");
        }
        Assert.assertTrue(sub.isDisposed());
    }

    private void setConfig(boolean singleResponse) {
        this.config = new MessageObjectHandlerConfig();
        this.config.setSingleResponse(singleResponse);
        this.config.setSendChannel(this.testChannelSend);
        this.config.setReturnChannel(this.testChannelSend);
    }

}