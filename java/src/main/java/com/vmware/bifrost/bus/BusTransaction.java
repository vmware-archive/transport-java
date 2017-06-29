package com.vmware.bifrost.bus;

/**
 * Copyright(c) VMware Inc. 2017
 */
public interface BusTransaction<T> {
    void unsubscribe();
    boolean isSubscribed();
    void tick(T payload);
    void error(T payload);
}
