package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class BusResponderTransactionTest {

    private EventBus bus;
    private String sendChannel = "#test-channel-send";

    @Before
    public void setUp() throws Exception {
        this.bus = new EventBusImpl();
    }

    @Test
    public void unsubscribe() throws Exception {
        BusResponderTransaction transaction = this.createTransaction();
        Assert.assertTrue(transaction.isSubscribed());
        transaction.unsubscribe();
        Assert.assertFalse(transaction.isSubscribed());

        transaction = this.createNullSubTransaction();
        Assert.assertFalse(transaction.isSubscribed());

        transaction.unsubscribe();
        Assert.assertFalse(transaction.isSubscribed());

        transaction = this.createNullResponderTransaction();
        Assert.assertTrue(transaction.isSubscribed());

        transaction.unsubscribe();
        Assert.assertFalse(transaction.isSubscribed());
    }

    @Test
    public void isSubscribed() throws Exception {
        BusResponderTransaction transaction = this.createTransaction();
        Assert.assertTrue(transaction.isSubscribed());
    }

    @Test
    public void tick() throws Exception {
        BusResponderTransaction transaction = this.createTransaction();
        TestObserver<Message> observer =
                this.bus.getApi().getResponseChannel(sendChannel, this.getClass().getName()).test();

        observer.assertSubscribed();
        observer.assertValueCount(0);

        transaction.tick("pop");
        observer.assertValueCount(1);
        transaction.tick("shop");
        observer.assertValueCount(2);

        transaction = this.createNullResponderTransaction();
        transaction.tick("tick");
        observer.assertValueCount(2);
        transaction.tick("tock");
        observer.assertValueCount(2);

        transaction = this.createTransaction();
        transaction.tick("hot");
        observer.assertValueCount(3);
        transaction.tick("rock");
        observer.assertValueCount(4);

    }

    @Test
    public void error() throws Exception {
        BusResponderTransaction transaction = this.createTransaction();
        TestObserver<Message> observer =
                this.bus.getApi().getErrorChannel(sendChannel, this.getClass().getName()).test();

        observer.assertSubscribed();
        observer.assertValueCount(0);

        transaction.error("pop");
        observer.assertValueCount(1);
        transaction.error("shop");
        observer.assertValueCount(2);

        transaction = this.createNullResponderTransaction();
        transaction.error("tick");
        observer.assertValueCount(2);
        transaction.error("tock");
        observer.assertValueCount(2);

        transaction = this.createTransaction();
        transaction.error("hot");
        observer.assertValueCount(3);
        transaction.error("rock");
        observer.assertValueCount(4);

    }

    private BusResponderTransaction createTransaction() {
        return new BusResponderTransaction(this.createSub(), this.createResponder());
    }

    private BusResponderTransaction createNullSubTransaction() {
        return new BusResponderTransaction(null, this.createResponder());
    }

    private BusResponderTransaction createNullResponderTransaction() {
        return new BusResponderTransaction(this.createSub(), null);
    }

    private Disposable createSub() {
        Observable<Message> chan = this.bus.getApi().getChannel(sendChannel, this.getClass().getName());
        return chan.subscribe();
    }

    private MessageResponder createResponder() {

        MessageObjectHandlerConfig config = new MessageObjectHandlerConfig();
        config.setSingleResponse(false);
        config.setSendChannel(sendChannel);
        config.setReturnChannel(sendChannel);
        MessageResponder responder = new MessageResponderImpl(config, this.bus);
        responder.generate(null);
        return responder;
    }

}
