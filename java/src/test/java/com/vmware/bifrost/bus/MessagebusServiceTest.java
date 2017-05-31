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
    public void checkChannels() {

        Observable<MessageObject> chan = this.bus.getChannel("#local-1", "test");

        MessageObject test1 = new MessageObject(MessageType.MessageTypeRequest, "cakes");
        MessageObject test2 = new MessageObject(MessageType.MessageTypeRequest, "biscuits");


        TestObserver<MessageObject> observer = chan.test();


        this.bus.send("#local-1", test1, "somewhere");
        this.bus.send("#local-1", test2, "out there");

        observer.assertSubscribed();
        observer.assertValues(test1, test2);

    }

    @Test
    public void checkMessagePolymorphism() {

        Observable<MessageObject> chan = this.bus.getChannel("#local-1", "test");

        MessageObject test1 = new MessageObject<String>(MessageType.MessageTypeRequest, "cakes");
        MessageObjectHandlerConfig test2 = new MessageObjectHandlerConfig<String>(MessageType.MessageTypeRequest, "biscuits");
        test2.setSendChannel("#local-2");
        test2.setReturnChannel("#local-2");
        test2.setSingleResponse(true);

        TestObserver<MessageObject> observer = chan.test();
        this.bus.send("#local-1", test1, "somewhere");
        this.bus.send("#local-1", test2, "out there");

        observer.assertSubscribed();
        observer.assertValues(test1, test2);

        for (MessageObject msg : observer.values()) {
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
        Observable<MessageObject> chan = this.bus.getChannel("#local-channel", "test");
        TestObserver<MessageObject> observer = chan.test();

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
        Observable<MessageObject> chan = this.bus.getChannel("#local-channel", "test");
        TestObserver<MessageObject> observer = chan.test();

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

}