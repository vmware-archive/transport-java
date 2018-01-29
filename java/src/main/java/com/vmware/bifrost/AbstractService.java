package com.vmware.bifrost;

/**
 * Copyright(c) VMware Inc. 2017
 */
public abstract class AbstractService {

    protected String getName() {
        return this.getClass().getTypeName();
    }
}
