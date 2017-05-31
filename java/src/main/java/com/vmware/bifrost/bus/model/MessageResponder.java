package com.vmware.bifrost.bus.model;

import org.reactivestreams.Subscription;

import java.util.function.Supplier;

/**
 * Copyright(c) VMware Inc. 2017
 */
public interface MessageResponder {
    public Subscription generate(Supplier<Boolean> generateSuccessResponse, Supplier<Boolean> generateErrorResponse);
    public void tick(Supplier<Boolean> payload);
    public boolean close();
    public boolean isClosed();
}
