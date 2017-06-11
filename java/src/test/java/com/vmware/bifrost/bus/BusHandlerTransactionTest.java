package com.vmware.bifrost.bus;

import com.sun.media.jfxmediaimpl.MediaDisposer;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class BusHandlerTransactionTest {

    MessagebusService bus;
    Logger logger;

    @Before
    public void setUp() throws Exception {
        this.bus = new MessagebusService();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    @Test
    public void unsubscribe() throws Exception {
        BusHandlerTransaction transaction = this.createTransaction();
        Assert.assertTrue(transaction.isSubscribed());
        transaction.unsubscribe();
        Assert.assertFalse(transaction.isSubscribed());

        transaction = this.createNullSubTransaction();
        Assert.assertFalse(transaction.isSubscribed());

        transaction.unsubscribe();
        Assert.assertFalse(transaction.isSubscribed());

        transaction = this.createNullHandlerTransaction();
        Assert.assertTrue(transaction.isSubscribed());

        transaction.unsubscribe();
        Assert.assertFalse(transaction.isSubscribed());


    }

    @Test
    public void isSubscribed() throws Exception {
        BusHandlerTransaction transaction = this.createTransaction();
        Assert.assertTrue(transaction.isSubscribed());
    }

    @Test
    public void tick() throws Exception {
        BusHandlerTransaction transaction = this.createTransaction();
        TestObserver<Message> observer =
                this.bus.getRequestChannel("#test-local", this.getClass().getName()).test();

        observer.assertSubscribed();
        observer.assertValueCount(0);

        transaction.tick("pop");
        observer.assertValueCount(1);
        transaction.tick("shop");
        observer.assertValueCount(2);

        transaction = this.createNullHandlerTransaction();
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
        BusHandlerTransaction transaction = this.createTransaction();
        TestObserver<Message> observer =
                this.bus.getErrorChannel("#test-local", this.getClass().getName()).test();

        observer.assertSubscribed();
        observer.assertValueCount(0);

        transaction.error("pop");
        observer.assertValueCount(1);
        transaction.error("shop");
        observer.assertValueCount(2);

        transaction = this.createNullHandlerTransaction();
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

    private BusHandlerTransaction createTransaction() {
        return new BusHandlerTransaction(this.createSub(), this.createHandler());
    }

    private BusHandlerTransaction createNullSubTransaction() {
        return new BusHandlerTransaction(null, this.createHandler());
    }

    private BusHandlerTransaction createNullHandlerTransaction() {
        return new BusHandlerTransaction(this.createSub(), null);
    }

    private Disposable createSub() {
        Observable<Message> chan = this.bus.getChannel("#test-local", this.getClass().getName());
        return chan.subscribe();
    }

    private MessageHandler createHandler() {

        MessageObjectHandlerConfig config = new MessageObjectHandlerConfig();
        config.setSingleResponse(false);
        config.setSendChannel("#test-local");
        config.setReturnChannel("#test-local");
        MessageHandler handler = new MessageHandlerImpl(false, config, this.bus);
        handler.handle(null);
        return handler;
    }

}