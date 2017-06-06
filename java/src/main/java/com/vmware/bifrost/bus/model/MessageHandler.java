package com.vmware.bifrost.bus.model;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * Copyright(c) VMware Inc. 2017
 */
public interface MessageHandler<T> {
    public Disposable handle(Consumer<Message> successHandler);
    public Disposable handle(Consumer<Message> successHandler, Consumer<Throwable> errorHandler);
    public void tick(T payload);
    public void close();
    public boolean isClosed();
}
