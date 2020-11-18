/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

public class BusStoreError {

    /**
     * Optional name of the store this error is related to.
     */
    public String storeName;

    /**
     * Optional id of the store item this error is related to.
     */
    public Object itemId;

    /**
     * The error message.
     */
    public String errorMsg;

    public BusStoreError(String errorMsg) {
        this(errorMsg, null);
    }

    public BusStoreError(String errorMsg, String storeName) {
        this(errorMsg, storeName, null);
    }

    public BusStoreError(String errorMsg, String storeName, Object itemId) {
        this.storeName = storeName;
        this.errorMsg = errorMsg;
        this.itemId = itemId;
    }
}
