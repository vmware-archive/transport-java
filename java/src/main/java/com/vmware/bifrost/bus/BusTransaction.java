package com.vmware.bifrost.bus;

/**
 * Copyright(c) VMware Inc. 2017
 */
public interface BusTransaction {
    public void unsubscribe();
    public boolean isSubscribed();
    public void tick(Object payload);
    public void error(Object payload);


}
