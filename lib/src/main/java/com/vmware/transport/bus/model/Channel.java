/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright(c) VMware Inc., 2017
 *
 * A Channel object represents a single channel on the message bus.
 * This enables many-to-many transactions. Anyone can send a packet on a stream, and anyone can subscribe to a stream.
 * There is no restriction on the object that is placed on a stream and its type is only known to the sender and the
 * receiver.
 *
 * The Channel stream allows for packets and errors to be transmitted and both can be received by subscribers.
 */

public class Channel {
    private String name;
    private AtomicInteger refCount = new AtomicInteger(0);
    private Boolean closed;

    private Subject<Message> streamObject;


    public Channel(String name) {
        this.name = name;
        streamObject = PublishSubject.create();
        closed = false;
    }

    public Integer getRefCount() {
        return refCount.get();
    }

    public String getName() {

        return name;
    }

    public Subject<Message> getStreamObject() {

        return streamObject;
    }

    public Boolean isClosed() {

        return closed;
    }

    public void send(Message messageObject) {
        streamObject.onNext(messageObject);
    }

    public void error(Error error) {
        streamObject.onError(error);
    }

    public void complete() {
        streamObject.onComplete();
        closed = true;
    }

    public Integer increment() {
        return refCount.incrementAndGet();
    }

    public Integer decrement() {
        return refCount.updateAndGet(i -> i > 0 ? i - 1 : i);
    }
}
