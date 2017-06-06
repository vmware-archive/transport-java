package com.vmware.bifrost.bus.model;

import io.reactivex.disposables.Disposable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Copyright(c) VMware Inc. 2017
 */
public interface MessageResponder<T> {
    public Disposable generate(Function<Message, T> generator);
    public void tick(T payload);
    public void close();
    public boolean isClosed();
}
