package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageType;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import io.reactivex.Observable;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessagebusServiceTest {

    private MessagebusService bus;

    @Before
    public void before() {
        this.bus = new MessagebusService();
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
        Logger logger = LoggerFactory.getLogger(this.getClass());

        Observable<Message> chan = this.bus.getChannel("#local-1", "test");

        Message test1 = new Message(MessageType.MessageTypeRequest, "cakes");
        Message test2 = new Message(MessageType.MessageTypeRequest, "biscuits");


        TestObserver<Message> observer = chan.test();


        this.bus.send("#local-1", test1, "somewhere");
        this.bus.send("#local-1", test2, "out there");

        observer.assertSubscribed();
        observer.assertValues(test1, test2);



    }

}