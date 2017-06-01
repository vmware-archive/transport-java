package com.vmware.bifrost.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.vmware.bifrost.bus.model.*;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessagebusServiceTest {

    private MessagebusService bus;
    private Logger logger;

    private JsonSchema schema;
    private ObjectMapper mapper;
    private JsonSchemaGenerator schemaGen;


    @Before
    public void before() throws Exception {
        this.bus = new MessagebusService();
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.mapper = new ObjectMapper();
        this.schemaGen = new JsonSchemaGenerator(mapper);
        this.schema = schemaGen.generateSchema(MessageSchema.class);

    }

    @Test
    public void createBus() {
        Assert.assertNotNull(this.bus);
    }

    @Test
    public void checkGetters() {
        Assert.assertNotNull(this.bus.getMonitor());
        Assert.assertNotNull(this.bus.getChannelMap());
        Assert.assertEquals(this.bus.getChannelMap().size(), 1);
        Assert.assertEquals(
                this.bus.getChannelObject("#fresh", this.getClass().getName()).getName(), "#fresh");
        Assert.assertEquals(this.bus.getChannelMap().size(), 2);
        Assert.assertNotNull(this.bus.getChannel("cats", this.getClass().getName()));


    }

    @Test
    public void checkChannelFilters() {

        Observable<Message> chan = this.bus.getChannel("#local-1", "test");

        MessageObject test1 = new MessageObject(MessageType.MessageTypeRequest, "cakes");
        MessageObject test2 = new MessageObject(MessageType.MessageTypeResponse, "biscuits");

        TestObserver<Message> observer = chan.test();

        this.bus.send("#local-1", test1, "ping");
        this.bus.send("#local-1", test2, "pong");

        observer.assertSubscribed();
        observer.assertValues(test1, test2);

        chan = this.bus.getRequestChannel("#local-1", "test");
        observer = chan.test();

        this.bus.send("#local-1", test1, "ping");
        this.bus.send("#local-1", test2, "pong");

        observer.assertSubscribed();
        observer.assertValues(test1);

        chan = this.bus.getResponseChannel("#local-1", "test");
        observer = chan.test();

        this.bus.send("#local-1", test1, "ping");
        this.bus.send("#local-1", test2, "pong");

        observer.assertSubscribed();
        observer.assertValues(test2);


    }

    @Test
    public void checkChannelClosing() {
        Observable<Message> chan1 = this.bus.getChannel("#local-channel", "test");
        Observable<Message> chan2 = this.bus.getChannel("#local-channel", "test");
        Channel chanRaw = this.bus.getChannelObject("#local-channel", "test");

        TestObserver<Message> observer1 = chan1.test();
        TestObserver<Message> observer2 = chan2.test();

        observer1.assertSubscribed();
        observer2.assertSubscribed();

        Assert.assertEquals((int)chanRaw.getRefCount(), 3);

        this.bus.close("#local-channel", "test");

        Assert.assertEquals((int)chanRaw.getRefCount(), 2);

        this.bus.close("#local-channel", "test");

        Assert.assertEquals((int)chanRaw.getRefCount(), 1);

        this.bus.close("#local-channel", "test");

        observer1.assertComplete();
        observer2.assertComplete();

    }

    @Test
    public void checkMessagePolymorphism() {

        Observable<Message> chan = this.bus.getChannel("#local-1", "test");

        MessageObject test1 = new MessageObject<String>(MessageType.MessageTypeRequest, "cakes");
        MessageObjectHandlerConfig test2 = new MessageObjectHandlerConfig<String>(MessageType.MessageTypeRequest, "biscuits");
        test2.setSendChannel("#local-2");
        test2.setReturnChannel("#local-2");
        test2.setSingleResponse(true);

        TestObserver<Message> observer = chan.test();
        this.bus.send("#local-1", test1, "somewhere");
        this.bus.send("#local-1", test2, "out there");

        observer.assertSubscribed();
        observer.assertValues(test1, test2);

        for (Message msg : observer.values()) {
            try {
                MessageObjectHandlerConfig config = (MessageObjectHandlerConfig) msg;
                Assert.assertEquals(config.getClass(), MessageObjectHandlerConfig.class);
                Assert.assertEquals(config.getPayload(), "biscuits");
            } catch (ClassCastException exp) {
                Assert.assertEquals(msg.getClass(), MessageObject.class);
                Assert.assertEquals(msg.getPayload(), "cakes");
            }

        }
    }

    @Test
    public void testSendRequest() {
        Observable<Message> chan = this.bus.getChannel("#local-channel", "test");
        TestObserver<Message> observer = chan.test();

        this.bus.sendRequest("#local-channel", "puppy!");

        observer.assertSubscribed();

        for (Message msg : observer.values()) {

            Assert.assertEquals(msg.getClass(), MessageObjectHandlerConfig.class);
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertEquals(msg.getPayload(), "puppy!");
            Assert.assertTrue(msg.isRequest());
            Assert.assertFalse(msg.isResponse());
        }
    }

    @Test
    public void testSendResponse() {
        Observable<Message> chan = this.bus.getChannel("#local-channel", "test");
        TestObserver<Message> observer = chan.test();

        this.bus.sendResponse("#local-channel", "kitty!");

        observer.assertSubscribed();

        for (Message msg : observer.values()) {

            Assert.assertEquals(msg.getClass(), MessageObjectHandlerConfig.class);
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertEquals(msg.getPayload(), "kitty!");
            Assert.assertFalse(msg.isRequest());
            Assert.assertTrue(msg.isResponse());
        }
    }

    @Test
    public void testSendError() {
        Observable<Message> chan = this.bus.getChannel("#local-channel", "test");
        TestObserver<Message> observer = chan.test();

        this.bus.sendError("#local-channel", "chickie!");

        observer.assertSubscribed();

        for (Message msg : observer.values()) {

            Assert.assertEquals(msg.getClass(), MessageObjectHandlerConfig.class);
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertEquals(msg.getPayload(), "chickie!");
            Assert.assertFalse(msg.isRequest());
            Assert.assertFalse(msg.isResponse());
            Assert.assertTrue(msg.isError());

        }
    }

    @Test
    public void testErrorBus() {
        Observable<Message> chan = this.bus.getChannel("#local-error", "test");
        TestObserver<Message> observer = chan.test();

        Error error = new Error("broke me glasses!");
        this.bus.error("#local-error", error);

        observer.assertSubscribed();
        observer.assertError(error);

        for (Message msg : observer.values()) {

            Assert.assertEquals(msg.getClass(), MessageObject.class);
            Assert.assertEquals(msg.getPayloadClass(), String.class);
            Assert.assertEquals(msg.getPayload(), "broke me glasses!");
            Assert.assertFalse(msg.isRequest());
            Assert.assertFalse(msg.isResponse());
            Assert.assertTrue(msg.isError());

        }
    }

}