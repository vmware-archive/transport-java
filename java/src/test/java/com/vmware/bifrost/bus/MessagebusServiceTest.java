package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageHandlerConfig;
import com.vmware.bifrost.bus.model.MessageType;
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


    @Before
    public void before() {
        this.bus = new MessagebusService();
        this.logger = LoggerFactory.getLogger(this.getClass());

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

        Observable<Message> chan = this.bus.getChannel("#local-1", "test");

        Message test1 = new Message(MessageType.MessageTypeRequest, "cakes");
        Message test2 = new Message(MessageType.MessageTypeRequest, "biscuits");


        TestObserver<Message> observer = chan.test();


        this.bus.send("#local-1", test1, "somewhere");
        this.bus.send("#local-1", test2, "out there");

        observer.assertSubscribed();
        observer.assertValues(test1, test2);

    }

    @Test
    public void checkMessagePolymorphism() {

        Observable<Message> chan = this.bus.getChannel("#local-1", "test");

        Message test1 = new Message<String>(MessageType.MessageTypeRequest, "cakes");
        MessageHandlerConfig test2 = new MessageHandlerConfig<String>(MessageType.MessageTypeRequest, "biscuits");
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
                MessageHandlerConfig config = (MessageHandlerConfig) msg;
                Assert.assertEquals(config.getClass(), MessageHandlerConfig.class);
                Assert.assertEquals(config.getPayload(), "biscuits");
            } catch (ClassCastException exp) {
                Assert.assertEquals(msg.getClass(), Message.class);
                Assert.assertEquals(msg.getPayload(), "cakes");
            }

        }
    }

}