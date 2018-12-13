/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Channel;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObject;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import com.vmware.bifrost.bus.model.MessageType;
import com.vmware.bifrost.bus.model.MonitorObject;
import com.vmware.bifrost.bus.model.MonitorType;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.Subject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.reactivex.Observable;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AnyOf.anyOf;

@SuppressWarnings("unchecked")
public class EventBusImplTest {

    private EventBus bus;

    private int counter;
    private int responsesWithIdCounter;
    private Message message;
    private int errors;
    private int errorsWithId;


    @Before
    public void before() throws Exception {
        this.bus = new EventBusImpl();

        this.counter = 0;
        this.errors = 0;
    }

    @Test
    public void createBus() {
        Assert.assertNotNull(this.bus);
    }

    @Test
    public void checkGetters() {
        Assert.assertNotNull(this.bus.getApi().getMonitor());
        Assert.assertNotNull(this.bus.getApi().getChannelMap());
        Assert.assertEquals(1, this.bus.getApi().getChannelMap().size());
        Assert.assertEquals("#fresh", this.bus.getApi().getChannelObject("#fresh", this.getClass().getName()).getName());
        Assert.assertEquals(this.bus.getApi().getChannelMap().size(), 2);
        Assert.assertNotNull(this.bus.getApi().getChannel("cats", this.getClass().getName()));
        Assert.assertTrue(this.bus.getApi().isLoggingEnabled());
        this.bus.getApi().enableMonitorDump(false);
        Assert.assertFalse(this.bus.getApi().isLoggingEnabled());

    }

    @Test
    public void checkChannelFilters() {

        Observable<Message> chan = this.bus.getApi().getChannel("#local-1", "test");

        MessageObject test1 = new MessageObject(MessageType.MessageTypeRequest, "cakes");
        MessageObject test2 = new MessageObject(MessageType.MessageTypeResponse, "biscuits");

        TestObserver<Message> observer = chan.test();

        this.bus.getApi().send("#local-1", test1, "ping");
        this.bus.getApi().send("#local-1", test2, "pong");

        observer.assertSubscribed();
        observer.assertValues(test1, test2);

        chan = this.bus.getApi().getRequestChannel("#local-1", "test");
        observer = chan.test();

        this.bus.getApi().send("#local-1", test1, "ping");
        this.bus.getApi().send("#local-1", test2, "pong");

        observer.assertSubscribed();
        observer.assertValues(test1);

        chan = this.bus.getApi().getResponseChannel("#local-1", "test");
        observer = chan.test();

        this.bus.getApi().send("#local-1", test1, "ping");
        this.bus.getApi().send("#local-1", test2, "pong");

        observer.assertSubscribed();
        observer.assertValues(test2);
    }

    @Test
    public void checkChannelClosing() {
        Observable<Message> chan1 = this.bus.getApi().getChannel("#local-channel", "test");
        Observable<Message> chan2 = this.bus.getApi().getChannel("#local-channel", "test");
        Channel chanRaw = this.bus.getApi().getChannelObject("#local-channel", "test");

        TestObserver<Message> observer1 = chan1.test();
        TestObserver<Message> observer2 = chan2.test();

        observer1.assertSubscribed();
        observer2.assertSubscribed();

        Assert.assertEquals(3, (int) chanRaw.getRefCount());

        this.bus.closeChannel("#local-channel", "test");

        Assert.assertEquals(2, (int) chanRaw.getRefCount());

        this.bus.closeChannel("#local-channel", "test");

        Assert.assertEquals(1, (int) chanRaw.getRefCount());

        this.bus.closeChannel("#local-channel", "test");

        observer1.assertComplete();
        observer2.assertComplete();

        this.bus.closeChannel("#no-channel", "test");
    }

    @Test
    public void checkMessagePolymorphism() {

        Observable<Message> chan = this.bus.getApi().getChannel("#local-1", "test");

        MessageObject<String> test1 = new MessageObject<>(MessageType.MessageTypeRequest, "cakes");
        MessageObjectHandlerConfig<String> test2 = new MessageObjectHandlerConfig<>(MessageType.MessageTypeRequest, "biscuits");
        test2.setSendChannel("#local-2");
        test2.setReturnChannel("#local-2");
        test2.setSingleResponse(true);

        TestObserver<Message> observer = chan.test();
        this.bus.getApi().send("#local-1", test1, "somewhere");
        this.bus.getApi().send("#local-1", test2, "out there");

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
    public void testSendRequestMessage() {
        Observable<Message> chan = this.bus.getApi().getChannel("#local-channel", "test");
        TestObserver<Message> observer = chan.test();

        this.bus.sendRequestMessage("#local-channel", "puppy!");

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
    public void testSendResponseMessage() {
        Observable<Message> chan = this.bus.getApi().getChannel("#local-channel", "test");
        TestObserver<Message> observer = chan.test();

        this.bus.sendResponseMessage("#local-channel", "kitty!");

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
    public void testSendErrorMessage() {
        Observable<Message> chan = this.bus.getApi().getChannel("#local-channel", "test");
        TestObserver<Message> observer = chan.test();

        this.bus.sendErrorMessage("#local-channel", "chickie!");

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
        Observable<Message> chan = this.bus.getApi().getChannel("#local-error", "test");
        TestObserver<Message> observer = chan.test();

        Error error = new Error("broke me glasses!");
        this.bus.getApi().error("#local-error", error);
        this.bus.getApi().error("#nochannel-error", error);

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
        Channel chan = this.bus.getApi().getChannelObject("#local-channel", "test");
        TestObserver<Message> observer = chan.getStreamObject().test();

        observer.assertSubscribed();

        this.bus.getApi().complete("#local-channel", "test");
        Assert.assertTrue(chan.isClosed());

        this.bus.sendResponseMessage("#local-channel", "kitty!");
        this.bus.sendResponseMessage("#local-channel", "kitty2 ");
        this.bus.sendResponseMessage("#local-channel", "kitty3");

        observer.assertValueCount(0);

        Assert.assertTrue(chan.isClosed());

        this.bus.getApi().complete("#nonexistent-channel", "test");
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
        TestObserver<Message> observer = this.bus.getApi().getErrorChannel(chan, "test").test();
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

        this.bus.sendErrorMessage(chan, "ouch!");
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
        TestObserver<Message> observer = this.bus.getApi().getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getApi().getResponseChannel(chan, "test").test();

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
        TestObserver<Message> observer = this.bus.getApi().getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getApi().getResponseChannel(chanret, "test").test();

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
        TestObserver<Message> observer = this.bus.getApi().getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getApi().getResponseChannel(chan, "test").test();
        TestObserver<Message> observer3 = this.bus.getApi().getErrorChannel(chan, "test").test();

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

        this.bus.sendErrorMessage(chan, error);

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

        Channel channelObj = this.bus.getApi().getChannelMap().get(chan);
        Assert.assertEquals(channelObj.getRefCount().intValue(), 1);

        BusTransaction handleTransaction = this.bus.requestOnce(
                chan,
                "coffee",
                (Message message) -> {
                    Assert.assertEquals(channelObj.getRefCount().intValue(), 2);
                    Assert.assertEquals("milk 1", message.getPayload());
                    Assert.assertEquals(String.class, message.getPayloadClass());
                    Assert.assertTrue(message.isResponse());
                    Assert.assertEquals(this.counter, 1);
                }
        );
        Assert.assertEquals(channelObj.getRefCount().intValue(), 0);
        Assert.assertFalse(this.bus.getApi().getChannelMap().containsKey(chan));
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

        // the error stream shouldn't be active as the channel should have been closed now.
        this.bus.sendErrorMessage(chan, "computer says no");
        Assert.assertEquals(this.counter, 1);
    }

    @Test
    public void testRequestOnceWithId() {

        UUID request1Uuid = UUID.randomUUID();

        String chan = "#local-simple-error";
        this.bus.requestOnceWithId(
              request1Uuid,
              chan,
              "request1",
              (Message message) -> {
                  this.responsesWithIdCounter++;
                  this.message = message;
              }
        );

        this.bus.listenStream(chan, (Message message) -> this.counter++ );

        Assert.assertEquals(this.bus.getApi().getChannelMap().get(chan).getRefCount().intValue(), 2);

        this.bus.sendResponseMessage(chan, "generel-response");
        Assert.assertEquals(this.counter, 1);
        Assert.assertEquals(this.responsesWithIdCounter, 0);

        this.bus.sendResponseMessageWithId(chan, "response-with-differen-uuid", UUID.randomUUID());
        Assert.assertEquals(this.counter, 2);
        Assert.assertEquals(this.responsesWithIdCounter, 0);

        this.bus.sendResponseMessageWithId(chan, "response-with-correct-uuid", request1Uuid);
        Assert.assertEquals(this.counter, 3);
        Assert.assertEquals(this.responsesWithIdCounter, 1);
        Assert.assertEquals(this.message.getPayload(), "response-with-correct-uuid");

        // After the first response, other responses should be ignored.
        this.bus.sendResponseMessageWithId(chan, "second-response-with-id", request1Uuid);
        Assert.assertEquals(this.responsesWithIdCounter, 1);

        Assert.assertEquals(this.bus.getApi().getChannelMap().get(chan).getRefCount().intValue(), 1);
    }

    @Test
    public void testRequestOnceWithIdErrorHandling() {

        UUID request1Uuid = UUID.randomUUID();

        String chan = "#local-simple-error";
        this.bus.requestOnceWithId(
              request1Uuid,
              chan,
              "request1",
              chan,
              (Message message) -> {
                  this.responsesWithIdCounter++;
                  this.message = message;
              },
              (Message message) -> {
                  this.errorsWithId++;
              }
        );

        this.bus.listenStream(
              chan,
              (Message message) -> this.counter++,
              (Message message) -> this.errors++);

        this.bus.sendErrorMessage(chan, "generel-response");
        Assert.assertEquals(this.errors, 1);
        Assert.assertEquals(this.errorsWithId, 0);
        Assert.assertEquals(this.responsesWithIdCounter, 0);

        this.bus.sendErrorMessageWithId(chan, "response-with-differen-uuid", UUID.randomUUID());
        Assert.assertEquals(this.errors, 2);
        Assert.assertEquals(this.responsesWithIdCounter, 0);
        Assert.assertEquals(this.errorsWithId, 0);

        this.bus.sendErrorMessageWithId(chan, "error", request1Uuid);
        Assert.assertEquals(this.errors, 3);
        Assert.assertEquals(this.responsesWithIdCounter, 0);
        Assert.assertEquals(this.errorsWithId, 1);

        // Verify that other errors with the same id are ignored.
        this.bus.sendErrorMessageWithId(chan, "error2", request1Uuid);
        Assert.assertEquals(this.errors, 4);
        Assert.assertEquals(this.errorsWithId, 1);

        this.bus.sendResponseMessageWithId(chan, "response", request1Uuid);
        Assert.assertEquals(this.responsesWithIdCounter, 0);
    }

    @Test
    public void testSendRequestMessageWithId() {

        UUID requestUuid = UUID.randomUUID();
        String chan = "#local-simple-error";

        this.bus.respondOnce(chan, (Message message) -> {
            Assert.assertEquals(message.getId(), requestUuid);
            return "response";
        });

        this.bus.listenStream(chan, (Message message) -> {
            Assert.assertEquals(message.getId(), requestUuid);
            Assert.assertEquals(message.getPayload(), "response");
            this.counter++;
        });

        this.bus.sendRequestMessageWithId(chan, "request", requestUuid);
        Assert.assertEquals(this.counter, 1);
    }

    @Test
    public void testRequestStreamWithId() {

        UUID requestUuid = UUID.randomUUID();
        String chan = "#local-simple";

        this.bus.respondStream(chan, (Message message) -> message.getPayload() + "-response");

        this.bus.listenStream(chan, (Message message) -> this.counter++);

        this.bus.requestStreamWithId(requestUuid, chan, "request1",
              (Message message) -> {
                  this.responsesWithIdCounter++;
                  this.message = message;
              });

        Assert.assertEquals(this.counter, 1);
        Assert.assertEquals(this.responsesWithIdCounter, 1);
        Assert.assertEquals(this.message.getPayload(), "request1-response");

        this.bus.sendRequestMessageWithId(chan, "request2", requestUuid);
        Assert.assertEquals(this.counter, 2);
        Assert.assertEquals(this.responsesWithIdCounter, 2);
        Assert.assertEquals(this.message.getPayload(), "request2-response");

        this.bus.sendRequestMessageWithId(chan, "request2", UUID.randomUUID());
        Assert.assertEquals(this.counter, 3);
        Assert.assertEquals(this.responsesWithIdCounter, 2);

        this.bus.sendResponseMessageWithId(chan, "response3", requestUuid);
        Assert.assertEquals(this.counter, 4);
        Assert.assertEquals(this.responsesWithIdCounter, 3);

        this.bus.sendRequestMessage(chan, "request3");
        Assert.assertEquals(this.counter, 5);
        Assert.assertEquals(this.responsesWithIdCounter, 3);
    }

    @Test
    public void testRequestStreamWithIdErrorHandling() {

        UUID requestUuid = UUID.randomUUID();
        String chan = "#local-simple-error";

        this.bus.respondStream(chan, (Message message) -> message.getPayload() + "-response");

        this.bus.listenStream(chan,
              (Message message) -> this.counter++,
              (Message message) -> this.errors++ );

        BusTransaction busTransaction = this.bus.requestStreamWithId(
              requestUuid,
              chan,
              "request1",
              chan,
              (Message message) -> {
                  this.responsesWithIdCounter++;
                  this.message = message;
              },
              (Message message) -> this.errorsWithId++);

        Assert.assertEquals(this.counter, 1);
        Assert.assertEquals(this.responsesWithIdCounter, 1);
        Assert.assertEquals(this.message.getPayload(), "request1-response");

        this.bus.sendErrorMessageWithId(chan, "error", requestUuid);
        Assert.assertEquals(this.counter, 1);
        Assert.assertEquals(this.errors, 1);
        Assert.assertEquals(this.responsesWithIdCounter, 1);
        Assert.assertEquals(this.errorsWithId, 1);

        this.bus.sendErrorMessageWithId(chan, "error2", UUID.randomUUID());
        Assert.assertEquals(this.errors, 2);
        Assert.assertEquals(this.errorsWithId, 1);

        this.bus.sendErrorMessageWithId(chan, "error3", requestUuid);
        Assert.assertEquals(this.errors, 3);
        Assert.assertEquals(this.errorsWithId, 2);

        busTransaction.unsubscribe();

        this.bus.sendErrorMessageWithId(chan, "error4", requestUuid);
        Assert.assertEquals(this.errors, 4);
        Assert.assertEquals(this.errorsWithId, 2);
    }

    @Test
    public void testResponseOnceError() {

        String chan = "#local-simple-error";

        TestObserver<Message> observer = this.bus.getApi().getErrorChannel(chan, "test").test();
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
        this.bus.sendErrorMessage(chan, "computer says no");

        Assert.assertEquals(observer.valueCount(), 1);
        Assert.assertEquals(this.counter, 0);
        this.bus.sendErrorMessage(chan, "computer says no again");

        Assert.assertEquals(observer.valueCount(), 2);
        Assert.assertEquals(this.counter, 0);

        this.bus.sendErrorMessage(chan, "computer says no yet again");
        Assert.assertEquals(observer.valueCount(), 3);

        sendTransaction.error("should not be ignored");
        Assert.assertEquals(observer.valueCount(), 4);
        Assert.assertEquals(this.counter, 0);

    }

    @Test
    public void testResponseStream() {

        String chan = "#local-simple";

        TestObserver<Message> observer = this.bus.getApi().getRequestChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getApi().getResponseChannel(chan, "test").test();

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

        TestObserver<Message> observer = this.bus.getApi().getRequestChannel(chanOut, "test").test();
        TestObserver<Message> observer2 = this.bus.getApi().getResponseChannel(chanReturn, "test").test();

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


        TestObserver<Message> observer = this.bus.getApi().getErrorChannel(chan, "test").test();
        TestObserver<Message> observer2 = this.bus.getApi().getResponseChannel(chan, "test").test();

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
    public void testListenStream() {
        String chan = "#local-simple";
        BusTransaction busTransaction = this.bus.listenStream(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        bus.sendResponseMessage(chan, "testMessage");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(this.message.getPayload(), "testMessage");

        bus.sendResponseMessage(chan, "testMessage2");
        Assert.assertEquals(2, this.counter);
        Assert.assertEquals(this.message.getPayload(), "testMessage2");

        bus.sendErrorMessage(chan, "error");
        Assert.assertEquals(2, this.counter);
        Assert.assertEquals(this.message.getPayload(), "testMessage2");

        busTransaction.unsubscribe();

        Assert.assertFalse(busTransaction.isSubscribed());
        bus.sendResponseMessage(chan, "testMessage3");

        Assert.assertEquals(2, this.counter);
        Assert.assertEquals(this.message.getPayload(), "testMessage2");
    }

    @Test
    public void testListenStreamWithErrorHandler() {
        String chan = "#local-simple";
        BusTransaction busTransaction = this.bus.listenStream(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              },
              (Message message) -> {
                  ++this.errors;
                  this.message = message;
              }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        bus.sendResponseMessage(chan, "testMessage");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(0, this.errors);
        Assert.assertEquals(this.message.getPayload(), "testMessage");

        bus.sendErrorMessage(chan, "error");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(1, this.errors);
        Assert.assertEquals(this.message.getPayload(), "error");

        busTransaction.unsubscribe();

        bus.sendErrorMessage(chan, "error2");
        bus.sendResponseMessage(chan, "testMessage2");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(1, this.errors);
    }

    @Test
    public void testListenStreamWithId() {
        String chan = "#local-simple";
        UUID id = UUID.randomUUID();
        BusTransaction busTransaction = this.bus.listenStream(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              },
              (Message message) -> {
                  ++this.errors;
                  this.message = message;
              },
              id
        );

        bus.sendResponseMessage(chan, "testMessage");
        Assert.assertEquals(0, this.counter);
        Assert.assertEquals(0, this.errors);

        bus.sendResponseMessageWithId(chan, "messageWithId", id);
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(0, this.errors);
        Assert.assertEquals(this.message.getPayload(), "messageWithId");

        bus.sendErrorMessage(chan, "error");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(0, this.errors);

        bus.sendErrorMessageWithId(chan, "errorWithId", id);
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(1, this.errors);
        Assert.assertEquals(this.message.getPayload(), "errorWithId");

        bus.sendResponseMessageWithId(chan, "messageWithId2", id);
        bus.sendErrorMessageWithId(chan, "errorWithId2", id);
        Assert.assertEquals(2, this.counter);
        Assert.assertEquals(2, this.errors);

        busTransaction.unsubscribe();

        bus.sendResponseMessageWithId(chan, "messageWithId2", id);
        bus.sendErrorMessageWithId(chan, "errorWithId2", id);
        Assert.assertEquals(2, this.counter);
        Assert.assertEquals(2, this.errors);
    }

    @Test
    public void testListenOnce() {
        String chan = "#local-simple";
        this.bus.listenOnce(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              }
        );

        bus.sendResponseMessage(chan, "testMessage");
        Assert.assertEquals(1, this.counter);

        bus.sendResponseMessage(chan, "testMessage2");
        Assert.assertEquals(1, this.counter);
    }

    @Test
    public void testListenOnceWithErrorHandling() {
        String chan = "#local-simple";
        this.bus.listenOnce(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              },
              (Message message) -> {
                  ++this.errors;
                  this.message = message;
              }
        );

        bus.sendErrorMessage(chan, "error");
        bus.sendResponseMessage(chan, "testMessage");
        Assert.assertEquals(1, this.errors);
        Assert.assertEquals(0, this.counter);

        bus.sendErrorMessage(chan, "error");
        bus.sendResponseMessage(chan, "testMessage2");
        Assert.assertEquals(1, this.errors);
        Assert.assertEquals(0, this.counter);
    }

    @Test
    public void testListenRequestStream() {
        String chan = "#local-simple";
        BusTransaction busTransaction = this.bus.listenRequestStream(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              }
        );

        Channel chanObj = this.bus.getApi().getChannelMap().get(chan);
        Assert.assertEquals(chanObj.getRefCount().intValue(), 1);

        Assert.assertTrue(busTransaction.isSubscribed());

        bus.sendRequestMessage(chan, "request1");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(this.message.getPayload(), "request1");

        // Verify that we ignore error & response messages
        bus.sendErrorMessage(chan, "error");
        bus.sendResponseMessage(chan, "response1");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(this.message.getPayload(), "request1");

        bus.sendRequestMessage(chan, "request2");
        Assert.assertEquals(2, this.counter);

        busTransaction.unsubscribe();

        bus.sendRequestMessage(chan, "request3");
        Assert.assertEquals(2, this.counter);
    }

    @Test
    public void testListenRequestStreamWithErrorHandler() {
        String chan = "#local-simple";
        BusTransaction busTransaction = this.bus.listenRequestStream(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              },
              (Message message) -> {
                  ++this.errors;
                  this.message = message;
              }
        );

        Assert.assertTrue(busTransaction.isSubscribed());

        bus.sendRequestMessage(chan, "request1");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(this.message.getPayload(), "request1");

        bus.sendErrorMessage(chan, "error1");
        bus.sendErrorMessage(chan, "error2");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(2, this.errors);
        Assert.assertEquals(this.message.getPayload(), "error2");

        // Verify that we ignore response messages
        bus.sendResponseMessage(chan, "response1");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(2, this.errors);

        busTransaction.unsubscribe();

        bus.sendRequestMessage(chan, "request2");
        bus.sendErrorMessage(chan, "error3");

        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(2, this.errors);
    }

    @Test
    public void testListenRequestStreamWithId() {
        String chan = "#local-simple";
        UUID id = UUID.randomUUID();
        this.bus.listenRequestStream(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              },
              (Message message) -> {
                  ++this.errors;
                  this.message = message;
              },
              id
        );

        bus.sendRequestMessage(chan, "request1");
        bus.sendErrorMessage(chan, "error1");
        Assert.assertEquals(0, this.counter);
        Assert.assertEquals(0, this.errors);

        bus.sendRequestMessageWithId(chan, "request1", id);
        bus.sendErrorMessageWithId(chan, "error1", id);
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(1, this.errors);
    }

    @Test
    public void testListenRequestOnce() {
        String chan = "#local-simple";
        BusTransaction busTransaction = this.bus.listenRequestOnce(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              }
        );

        Channel chanObj = this.bus.getApi().getChannelMap().get(chan);
        Assert.assertEquals(chanObj.getRefCount().intValue(), 1);

        Assert.assertTrue(busTransaction.isSubscribed());

        bus.sendRequestMessage(chan, "request1");
        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(this.message.getPayload(), "request1");

        // Verify that subsequent requests & errors are ignored.
        bus.sendErrorMessage(chan, "error");
        bus.sendRequestMessage(chan, "request2");
        bus.sendResponseMessage(chan, "response1");

        Assert.assertEquals(1, this.counter);
        Assert.assertEquals(chanObj.getRefCount().intValue(), 0);
    }

    @Test
    public void testListenRequestOnceWithErrorHandling() {
        String chan = "#local-simple";
        BusTransaction busTransaction = this.bus.listenRequestOnce(
              chan,
              (Message message) -> {
                  ++this.counter;
                  this.message = message;
              },
              (Message message) -> {
                  ++this.errors;
                  this.message = message;
              }
        );

        Channel chanObj = this.bus.getApi().getChannelMap().get(chan);
        Assert.assertEquals(chanObj.getRefCount().intValue(), 1);

        bus.sendErrorMessage(chan, "error");
        bus.sendRequestMessage(chan, "request1");
        Assert.assertEquals(1, this.errors);
        Assert.assertEquals(0, this.counter);

        // Verify that subsequent requests & errors are ignored.
        bus.sendErrorMessage(chan, "error");
        bus.sendRequestMessage(chan, "request2");
        bus.sendResponseMessage(chan, "response1");

        Assert.assertEquals(1, this.errors);
        Assert.assertEquals(0, this.counter);

        Assert.assertEquals(chanObj.getRefCount().intValue(), 0);
    }

    @Test
    public void testCloseChannel() {
        String chan = "#local-simple";
        this.bus.requestOnce(chan, "request1", (Message message) -> ++this.counter);

        Channel chanObj = this.bus.getApi().getChannelMap().get(chan);
        Assert.assertEquals(chanObj.getRefCount().intValue(), 1);

        this.bus.requestStream(chan, "request2", (Message message) -> ++this.counter);

        Assert.assertEquals(chanObj.getRefCount().intValue(), 2);

        this.bus.closeChannel(chan, "test");
        Assert.assertEquals(chanObj.getRefCount().intValue(), 1);
        Assert.assertTrue(this.bus.getApi().getChannelMap().containsKey(chan));

        this.bus.closeChannel(chan, "test");
        Assert.assertEquals(chanObj.getRefCount().intValue(), 0);
        Assert.assertFalse(this.bus.getApi().getChannelMap().containsKey(chan));
    }

    @Test
    public void testCreateTransaction() {
        Transaction transaction = this.bus.createTransaction();
        Assert.assertNotNull(transaction);
    }

    @Test
    public void testMonitor() {

        String chan = "#scooby-do";
        String chan2 = "#scrappy-do";

        Subject<Message> monitorStream = bus.getApi().getMonitor();
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

        bus.getApi().getChannel(chan, this.getClass().getName());
        observerMonitor.assertValueCount(1);

        TestObserver<Message> observerStream = bus.getApi().getChannel(chan, "test").test();
        observerStream.assertValueCount(0);
        observerMonitor.assertValueCount(2);

        bus.sendRequestMessage(chan, "chickie");
        observerMonitor.assertValueCount(3);
        observerStream.assertValueCount(1);

        bus.sendResponseMessage(chan, "chickie");
        observerMonitor.assertValueCount(4);
        observerStream.assertValueCount(2);

        bus.sendErrorMessage(chan, "maggie");
        observerMonitor.assertValueCount(5);
        observerStream.assertValueCount(3);

        bus.sendResponseMessage("no-chan!", "foxypop");
        observerMonitor.assertValueCount(6);
        observerStream.assertValueCount(3);

        TestObserver<Message> observerStream2 = bus.getApi().getChannel(chan, "test").test();
        observerMonitor.assertValueCount(7);
        observerStream2.assertValueCount(0);

        bus.closeChannel(chan, "test");
        observerMonitor.assertValueCount(8);
        observerStream.assertValueCount(3);

        TestObserver<Message> observerStream3 = bus.getApi().getChannel(chan2, "test").test();
        observerMonitor.assertValueCount(9);
        observerStream3.assertValueCount(0);
        bus.getApi().complete(chan2, "test");


    }

}