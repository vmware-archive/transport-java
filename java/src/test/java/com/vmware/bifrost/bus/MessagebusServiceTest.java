package com.vmware.bifrost.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.vmware.bifrost.bus.model.*;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.Subject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AnyOf.anyOf;

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
    private int errors;


    @Before
    public void before() throws Exception {
        this.bus = new MessagebusService();
        this.logger = LoggerFactory.getLogger(this.getClass());

        this.mapper = new ObjectMapper();
        this.schemaGen = new JsonSchemaGenerator(mapper);
        this.schema = schemaGen.generateSchema(MessageSchema.class);
        this.counter = 0;
        this.errors = 0;

    }

    @Test
    public void createBus() {
        Assert.assertNotNull(this.bus);
    }

    @Test
    public void checkGetters() {
        Assert.assertNotNull(this.bus.getMonitor());
        Assert.assertNotNull(this.bus.getChannelMap());
        Assert.assertEquals(1, this.bus.getChannelMap().size());
        Assert.assertEquals("#fresh", this.bus.getChannelObject("#fresh", this.getClass().getName()).getName());
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

        Assert.assertEquals(3, (int) chanRaw.getRefCount());

        this.bus.close("#local-channel", "test");

        Assert.assertEquals(2, (int) chanRaw.getRefCount());

        this.bus.close("#local-channel", "test");

        Assert.assertEquals(1, (int) chanRaw.getRefCount());

        this.bus.close("#local-channel", "test");

        observer1.assertComplete();
        observer2.assertComplete();

        this.bus.close("#no-channel", "test");

    }

    @Test
    public void checkMessagePolymorphism() {

        Observable<Message> chan = this.bus.getChannel("#local-1", "test");

        MessageObject<String> test1 = new MessageObject<>(MessageType.MessageTypeRequest, "cakes");
        MessageObjectHandlerConfig<String> test2 = new MessageObjectHandlerConfig<>(MessageType.MessageTypeRequest, "biscuits");
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
                Assert.assertEquals("biscuits", config.getPayload());
            } catch (ClassCastException exp) {
                Assert.assertEquals(msg.getClass(), MessageObject.class);
                Assert.assertEquals("cakes", msg.getPayload());
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
            Assert.assertEquals("puppy!", msg.getPayload());
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
            Assert.assertEquals("kitty!", msg.getPayload());
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
            Assert.assertEquals("chickie!", msg.getPayload());
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
                    Assert.assertEquals("woof woof", message.getPayload());
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isRequest());
                    return "meow meow " + this.counter;
                }
        );

        this.bus.requestOnce(
                chan,
                "woof woof",
                (Message message) -> {
                    Assert.assertEquals("meow meow 1", message.getPayload());
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
                    Assert.assertEquals("what pups?", message.getPayload());
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
                    Assert.assertEquals("chickie and fox", message.getPayload());
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
        TestObserver<Message> observer = this.bus.getErrorChannel(chan, "test").test();
        observer.assertSubscribed();
        observer.assertValueCount(0);

        responder.error("ignore me, not subscribed yet");

        observer.assertValueCount(0);

        responder.generate(
                (Message message) -> {
                    Assert.assertEquals("and who else?", message.getPayload());
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
                    Assert.assertEquals("maggie too!", message.getPayload());
                    Assert.assertEquals(message.getPayloadClass(), String.class);
                    Assert.assertTrue(message.isResponse());
                    Assert.assertFalse(message.isError());
                },
                (Message message) -> {
                    Assert.assertEquals("ouch!", message.getPayload());
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
        TestObserver<Message> observer3 = this.bus.getErrorChannel(chan, "test").test();

        observer.assertSubscribed();
        observer2.assertSubscribed();
        observer3.assertSubscribed();

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
                    this.errors++;
                    Assert.assertEquals(error, message.getPayload());
                }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.tick(question + 2);
        busTransaction.tick(question + 3);
        busTransaction.tick(question + 4);

        this.bus.sendError(chan, error);

        observer3.assertValueCount(1);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.error(error);
        busTransaction.error(error);

        Assert.assertEquals(3, this.errors);

        observer3.assertValueCount(3);

        busTransaction.unsubscribe();

        Assert.assertFalse(busTransaction.isSubscribed());

        busTransaction.tick(question + 5);
        busTransaction.tick(question + 6);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

        busTransaction.error(error);
        busTransaction.error(error);

        Assert.assertEquals(3, this.errors);

    }

    @Test
    public void testResponseOnce() {

        String chan = "#local-simple";

        BusTransaction sendTransaction = this.bus.respondOnce(
                chan,
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals("coffee", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isRequest());
                    return "milk " + this.counter;
                }
        );

        BusTransaction handleTransaction = this.bus.requestOnce(
                chan,
                "coffee",
                (Message message) -> {
                    Assert.assertEquals("milk 1", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isResponse());
                    Assert.assertEquals(this.counter, 1);
                }
        );

        Assert.assertFalse(sendTransaction.isSubscribed());
        Assert.assertFalse(handleTransaction.isSubscribed());

    }

    @Test
    public void testResponseOnceDifferentChannel() {

        String sendChan = "#local-simple";
        String recvChan = "#local-return";

        BusTransaction sendTransaction = this.bus.respondOnce(
                sendChan,
                recvChan,
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals("chocolate pie", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isRequest());
                    return "chocolate pie " + this.counter;
                }
        );

        BusTransaction handleTransaction = this.bus.requestOnce(
                sendChan,
                "chocolate pie",
                recvChan,
                (Message message) -> {
                    Assert.assertEquals("chocolate pie 1", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isResponse());
                    Assert.assertEquals(this.counter, 1);
                }
        );

        Assert.assertFalse(sendTransaction.isSubscribed());
        Assert.assertFalse(handleTransaction.isSubscribed());

    }

    @Test
    public void testRequestResponseOnceError() {

        String chan = "#local-simple-error";

        BusTransaction sendTransaction = this.bus.respondOnce(
                chan,
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals("chick chick chickie", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isRequest());
                    return "maggie and fox " + this.counter;
                }
        );

        BusTransaction handleTransaction = this.bus.requestOnce(
                chan,
                "chick chick chickie",
                chan,
                (Message message) -> {
                    Assert.assertEquals("maggie and fox 1", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isResponse());
                    Assert.assertEquals(this.counter, 1);
                },
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals("computer says no", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isError());
                    Assert.assertEquals(this.counter, 2);
                }
        );

        Assert.assertFalse(sendTransaction.isSubscribed());
        Assert.assertFalse(handleTransaction.isSubscribed());

        // tick and error should not work.
        handleTransaction.error("something else");
        Assert.assertEquals(this.counter, 1);

        // any further errors or messages won't be handled.
        handleTransaction.error("another one here");
        Assert.assertEquals(this.counter, 1);

        handleTransaction.tick("should not be handled");
        Assert.assertEquals(this.counter, 1);

        sendTransaction.tick("should be ignored also");
        Assert.assertEquals(this.counter, 1);

        // the error stream is still active and can still pick up a single error however.
        this.bus.sendError(chan, "computer says no");
        Assert.assertEquals(this.counter, 2);

        this.bus.sendError(chan, "should be ignored");
        Assert.assertEquals(this.counter, 2);

    }

    @Test
    public void testResponseOnceError() {

        String chan = "#local-simple-error";

        TestObserver<Message> observer = this.bus.getErrorChannel(chan, "test").test();
        observer.assertSubscribed();

        BusTransaction sendTransaction = this.bus.respondOnce(
                chan,
                (Message message) -> {
                    this.counter++;
                    Assert.assertEquals("did it work?", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isRequest());
                    return "it did work! " + this.counter;
                }
        );

        Assert.assertEquals(this.counter, 0);
        this.bus.sendError(chan, "computer says no");

        Assert.assertEquals(observer.valueCount(), 1);
        Assert.assertEquals(this.counter, 0);
        this.bus.sendError(chan, "computer says no again");

        Assert.assertEquals(observer.valueCount(), 2);
        Assert.assertEquals(this.counter, 0);

        this.bus.sendError(chan, "computer says no yet again");
        Assert.assertEquals(observer.valueCount(), 3);

        sendTransaction.error("should not be ignored");
        Assert.assertEquals(observer.valueCount(), 4);
        Assert.assertEquals(this.counter, 0);

    }

    @Test
    public void testResponseStream() {

        String chan = "#local-simple";

        TestObserver<Message> observer = this.bus.getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getResponseChannel(chan, "test").test();

        BusTransaction responseTransaction = this.bus.respondStream(
                chan,
                (Message message) -> ++this.counter
        );

        BusTransaction busTransaction = this.bus.requestStream(
                chan,
                "ignored",
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), this.counter);
                    Assert.assertTrue(message.isResponse());
                }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.tick("anything");
        busTransaction.tick("anything");

        Assert.assertEquals(3, this.counter);

        busTransaction.tick("anything");

        Assert.assertEquals(4, this.counter);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.unsubscribe();

        Assert.assertFalse(busTransaction.isSubscribed());

        Assert.assertTrue(responseTransaction.isSubscribed());

        responseTransaction.unsubscribe();

        Assert.assertFalse(responseTransaction.isSubscribed());

    }

    @Test
    public void testResponseStreamDifferentReturnChan() {

        String chanOut = "#local-simple";
        String chanReturn = "#local-simple-return";

        TestObserver<Message> observer = this.bus.getRequestChannel(chanOut, "test").test();
        TestObserver<Message> observer2 = this.bus.getResponseChannel(chanReturn, "test").test();

        BusTransaction responseTransaction = this.bus.respondStream(
                chanOut,
                chanReturn,
                (Message message) -> ++this.counter
        );

        BusTransaction busTransaction = this.bus.requestStream(
                chanOut,
                chanReturn,
                "ignored",
                (Message message) -> {
                    Assert.assertEquals(message.getPayload(), this.counter);
                    Assert.assertTrue(message.isResponse());
                }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.tick("anything");
        busTransaction.tick("anything");

        Assert.assertEquals(3, this.counter);

        busTransaction.tick("anything");

        Assert.assertEquals(4, this.counter);

        observer.assertValueCount(4);
        observer2.assertValueCount(4);

        Assert.assertTrue(busTransaction.isSubscribed());

        busTransaction.unsubscribe();

        Assert.assertFalse(busTransaction.isSubscribed());

        Assert.assertTrue(responseTransaction.isSubscribed());

        responseTransaction.unsubscribe();

        Assert.assertFalse(responseTransaction.isSubscribed());

    }

    @Test
    public void testResponseStreamError() {

        String chan = "#local-simple-error";

        BusTransaction responseTransaction = this.bus.respondStream(
                chan,
                (Message message) -> ++this.counter
        );


        TestObserver<Message> observer = this.bus.getErrorChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getResponseChannel(chan, "test").test();

        observer.assertSubscribed();


        Assert.assertEquals(0, this.counter);
        responseTransaction.error("computer says no");

        Assert.assertEquals(observer.valueCount(), 1);
        Assert.assertEquals(0, this.counter);

        responseTransaction.error("computer says no again");

        Assert.assertEquals(observer.valueCount(), 2);
        Assert.assertEquals(0, this.counter);

        responseTransaction.error("computer says no yet again");
        Assert.assertEquals(observer.valueCount(), 3);
        Assert.assertEquals(0, this.counter);

        responseTransaction.tick("a thing!");
        Assert.assertEquals(0, this.counter);
        Assert.assertEquals(observer2.valueCount(), 1);

    }

    @Test
    public void testMonitor() {

        String chan = "#scooby-do";
        String chan2 = "#scrappy-do";

        Subject<Message> monitorStream = bus.getMonitor();
        TestObserver<Message> observerMonitor = monitorStream.test();

        monitorStream.subscribe(
                (Message message) -> {
                    Assert.assertNotNull(message);
                    Assert.assertNotNull(message.getPayloadClass());
                    Assert.assertNotNull(message.getPayload());
                    Assert.assertEquals(message.getPayloadClass(), MonitorObject.class);

                    MonitorObject mo = (MonitorObject) message.getPayload();

                    if (mo.isNewChannel()) {
                        Assert.assertThat(mo.getChannel(), anyOf(containsString(chan),
                                containsString(chan2)));
                    }

                    if (mo.getType().equals(MonitorType.MonitorData)) {
                        Assert.assertEquals("chickie", mo.getData().toString());
                    }

                    if (mo.getType().equals(MonitorType.MonitorError)) {
                        Assert.assertEquals("maggie", mo.getData().toString());
                    }

                    if (mo.getType().equals(MonitorType.MonitorDropped)) {
                        Assert.assertEquals("foxypop", mo.getData().toString());
                    }

                    if (mo.getType().equals(MonitorType.MonitorCloseChannel)) {
                        Assert.assertEquals("close [#scooby-do] 2 references remaining",
                                mo.getData().toString());
                    }

                    if (mo.getType().equals(MonitorType.MonitorCompleteChannel)) {
                        Assert.assertEquals("completed [#scrappy-do]",
                                mo.getData().toString());
                    }

//                    if (mo.getType().equals(MonitorType.MonitorDestroyChannel)) {
//                        Assert.assertEquals("destroyed [#scooby-do]",
//                                mo.getData().toString());
//                    }

                }
        );

        bus.getChannel(chan, this.getClass().getName());
        observerMonitor.assertValueCount(1);

        TestObserver<Message> observerStream = bus.getChannel(chan, "test").test();
        observerStream.assertValueCount(0);
        observerMonitor.assertValueCount(2);

        bus.sendRequest(chan, "chickie");
        observerMonitor.assertValueCount(3);
        observerStream.assertValueCount(1);

        bus.sendResponse(chan, "chickie");
        observerMonitor.assertValueCount(4);
        observerStream.assertValueCount(2);

        bus.sendError(chan, "maggie");
        observerMonitor.assertValueCount(5);
        observerStream.assertValueCount(3);

        bus.sendResponse("no-chan!", "foxypop");
        observerMonitor.assertValueCount(6);
        observerStream.assertValueCount(3);

        TestObserver<Message> observerStream2 = bus.getChannel(chan, "test").test();
        observerMonitor.assertValueCount(7);
        observerStream2.assertValueCount(0);

        bus.close(chan, "test");
        observerMonitor.assertValueCount(8);
        observerStream.assertValueCount(3);

        TestObserver<Message> observerStream3 = bus.getChannel(chan2, "test").test();
        observerMonitor.assertValueCount(9);
        observerStream3.assertValueCount(0);
        bus.complete(chan2, "test");


    }


}