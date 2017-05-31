package com.vmware.bifrost.bus.model;

import org.reactivestreams.Subscription;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Copyright(c) VMware Inc. 2017
 */
public interface MessageHandler {
    public Subscription handle(Consumer<Boolean> successHandler);
    public Subscription handle(Consumer<Boolean> successHandler, Consumer<Boolean> errorHandler);
    public void tick(Supplier<Object> payload);
    public void close();
    public boolean isClosed();
}
