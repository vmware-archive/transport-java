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
@SuppressWarnings("unchecked")
public class MessagebusServiceTest {

    private MessagebusService bus;
    private Logger logger;

    private JsonSchema schema;
    private ObjectMapper mapper;
    private JsonSchemaGenerator schemaGen;
    private int counter;


    @Before
    public void before() throws Exception {
        this.bus = new MessagebusService();
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.mapper = new ObjectMapper();
        this.schemaGen = new JsonSchemaGenerator(mapper);
        this.schema = schemaGen.generateSchema(MessageSchema.class);
        this.counter = 0;

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
        Assert.assertTrue(this.bus.isLoggingEnabled());
        this.bus.enableMonitorDump(false);
        Assert.assertFalse(this.bus.isLoggingEnabled());

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

        Assert.assertEquals((int) chanRaw.getRefCount(), 3);

        this.bus.close("#local-channel", "test");

        Assert.assertEquals((int) chanRaw.getRefCount(), 2);

        this.bus.close("#local-channel", "test");

        Assert.assertEquals((int) chanRaw.getRefCount(), 1);

        this.bus.close("#local-channel", "test");

        observer1.assertComplete();
        observer2.assertComplete();

        this.bus.close("#no-channel", "test");

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
            Assert.assertTrue(msg.isError());

        }
    }

    @Test
    public void testErrorBus() {
        Observable<Message> chan = this.bus.getChannel("#local-error", "test");
        TestObserver<Message> observer = chan.test();

        Error error = new Error("broke me glasses!");
        this.bus.error("#local-error", error);
        this.bus.error("#nochannel-error", error);

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

    @Test
    public void testChannelCompletion() {
        Channel chan = this.bus.getChannelObject("#local-channel", "test");
        TestObserver<Message> observer = chan.getStreamObject().test();

        observer.assertSubscribed();

        this.bus.complete("#local-channel", "test");
        Assert.assertTrue(chan.isClosed());

        this.bus.sendResponse("#local-channel", "kitty!");
        this.bus.sendResponse("#local-channel", "kitty2 ");
        this.bus.sendResponse("#local-channel", "kitty3");

        observer.assertValueCount(0);

        Assert.assertTrue(chan.isClosed());

        this.bus.complete("#nonexistent-channel", "test");

    }

    @Test
    public void testRequestResponseOnce() {

        String chan = "#local-simple";

        MessageObjectHandlerConfig<String> config = new MessageObjectHandlerConfig<>();
        config.setSingleResponse(true);
        config.setSendChannel(chan);
        config.setReturnChannel(chan);

        MessageResponder<String> responder = new MessageResponderImpl(false, config, this.bus);

        responder.generate(
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals(message.getPayload(), "woof woof");
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isRequest());
                    return "meow meow " + this.counter;
                }
        );

        this.bus.requestOnce(
                chan,
                "woof woof",
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), "meow meow 1");
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isResponse());
                }
        );

    }

    @Test
    public void testRequestResponseOnceDifferentChannel() {

        String chan = "#local-simple";
        String chanret = "#local-simple-return";

        MessageObjectHandlerConfig<String> config = new MessageObjectHandlerConfig<>();
        config.setSingleResponse(true);
        config.setSendChannel(chan);
        config.setReturnChannel(chanret);

        MessageResponder<String> responder = new MessageResponderImpl(false, config, this.bus);

        responder.generate(
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), "what pups?");
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isRequest());
                    return "chickie and fox";
                }
        );

        this.bus.requestOnce(
                chan,
                "what pups?",
                chanret,
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), "chickie and fox");
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isResponse());
                }
        );

    }

    @Test
    public void testRequestErrorReponseOnce() {

        String chan = "#local-simple";

        MessageObjectHandlerConfig<String> config = new MessageObjectHandlerConfig<>();
        config.setSingleResponse(true);
        config.setSendChannel(chan);
        config.setReturnChannel(chan);

        MessageResponder<String> responder = new MessageResponderImpl(false, config, this.bus);

        responder.generate(
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), "and who else?");
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isRequest());
                    return "maggie too!";
                }
        );

        this.bus.requestOnce(
                chan,
                "and who else?",
                chan,
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), "maggie too!");
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isResponse());
                    Assert.assertFalse(message.isError());
                },
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), "ouch!");
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isError());
                }
        );

        this.bus.sendError(chan, "ouch!");

    }

    @Test
    public void testRequestResponseStream() {

        String chan = "#local-stream";

        MessageObjectHandlerConfig<String> config = new MessageObjectHandlerConfig<>();
        config.setSingleResponse(false);
        config.setSendChannel(chan);
        config.setReturnChannel(chan);

        String question = "where is the kitty";
        String answer = "under the bed";

        MessageResponder<String> responder = new MessageResponderImpl(false, config, this.bus);
        TestObserver<Message> observer = this.bus.getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getResponseChannel(chan, "test").test();

        observer.assertSubscribed();
        observer2.assertSubscribed();

        responder.generate(
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals(question + this.counter, message.getPayload());
                    Assert.assertTrue(message.isRequest());
                    return answer + this.counter;
                }
        );

        BusTransaction busTransaction = this.bus.requestStream(
                chan,
                question + 1,
                (Message message) -> {
                    Assert.assertEquals(answer + this.counter, message.getPayload());
                    Assert.assertTrue(message.isResponse());
                }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.tick(question + 2);
        busTransaction.tick(question + 3);
        busTransaction.tick(question + 4);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.unsubscribe();

        Assert.assertFalse(busTransaction.isSubscribed());

    }

    @Test
    public void testRequestResponseStreamDiffChan() {

        String chan = "#local-stream";
        String chanret = "#local-stream-ret";

        MessageObjectHandlerConfig<String> config = new MessageObjectHandlerConfig<>();
        config.setSingleResponse(false);
        config.setSendChannel(chan);
        config.setReturnChannel(chanret);

        String question = "shipoopi";
        String answer = "the girl who's hard to get";

        MessageResponder<String> responder = new MessageResponderImpl(false, config, this.bus);
        TestObserver<Message> observer = this.bus.getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getResponseChannel(chanret, "test").test();

        observer.assertSubscribed();
        observer2.assertSubscribed();

        responder.generate(
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals(question + this.counter, message.getPayload());
                    return answer + this.counter;
                }
        );

        BusTransaction busTransaction = this.bus.requestStream(
                chan,
                question + 1,
                chanret,
                (Message message) -> {
                    Assert.assertEquals(answer + this.counter, message.getPayload());
                }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.tick(question + 2);
        busTransaction.tick(question + 3);
        busTransaction.tick(question + 4);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.unsubscribe();

        Assert.assertFalse(busTransaction.isSubscribed());

    }


    @Test
    public void testRequestResponseStreamError() {

        String chan = "#local-stream";

        MessageObjectHandlerConfig<String> config = new MessageObjectHandlerConfig<>();
        config.setSingleResponse(false);
        config.setSendChannel(chan);
        config.setReturnChannel(chan);

        String question = "any more beer?";
        String answer = "nope, we drank it all";
        String error = "bleh!";

        MessageResponder<String> responder = new MessageResponderImpl(false, config, this.bus);
        TestObserver<Message> observer = this.bus.getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getResponseChannel(chan, "test").test();

        observer.assertSubscribed();
        observer2.assertSubscribed();

        responder.generate(
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals(question + this.counter, message.getPayload());
                    return answer + this.counter;
                }
        );

        BusTransaction busTransaction = this.bus.requestStream(
                chan,
                question + 1,
                chan,
                (Message message) -> {
                    Assert.assertEquals(answer + this.counter, message.getPayload());
                },
                (Message message) -> {
                    Assert.assertEquals(error, message.getPayload());
                }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.tick(question + 2);
        busTransaction.tick(question + 3);
        busTransaction.tick(question + 4);

        this.bus.sendError(chan, error);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.unsubscribe();

        Assert.assertFalse(busTransaction.isSubscribed());

        busTransaction.tick(question + 5);
        busTransaction.tick(question + 6);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

    }

}