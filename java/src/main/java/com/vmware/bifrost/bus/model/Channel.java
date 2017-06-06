package com.vmware.bifrost.bus.model;

import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Copyright(c) VMware Inc., 2017
 * <p>
 * <p>
 * A Channel object represents a single channel on the message bus.
 * This enables many-to-many transactions. Anyone can send a packet on a stream, and anyone can subscribe to a stream.
 * There is no restriction on the object that is placed on a stream and its type is only known to the sender and the
 * receiver.
 * <p>
 * The Channel stream allows for packets and errors to be transmitted and both can be received by subscribers.
 */

public class Channel {
    private String name;
    private Integer refCount;
    private Boolean closed;
    private Boolean galactic;

    private Subject<Message> streamObject;


    public Channel(String name) {
        this.name = name;
        refCount = 0;
        streamObject = PublishSubject.create();
        closed = false;
        galactic = false;
    }

    public Integer getRefCount() {

        return refCount;
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
        return ++refCount;
    }

    public Integer decrement() {
        if (refCount > 0) {
            return --refCount;
        }
        return refCount;
    }

    public Channel setGalatic() {
        galactic = true;
        return this;
    }

    public Channel setPrivate() {
        this.galactic = false;
        return this;
    }

    public Boolean isGalactic() {
        return galactic;
    }
}
