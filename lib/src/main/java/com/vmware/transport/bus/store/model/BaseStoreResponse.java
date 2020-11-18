/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

public abstract class BaseStoreResponse {

    public final String storeId;

    public final String responseType;

    /**
     * The current version of the store
     */
    public final long storeVersion;

    BaseStoreResponse(String responseType, String storeId, long storeVersion) {
        this.storeId = storeId;
        this.responseType = responseType;
        this.storeVersion = storeVersion;
    }
}
