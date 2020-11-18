/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

public class UpdateStoreResponse extends BaseStoreResponse {

    public Object itemId;

    public Object newItemValue;

    public UpdateStoreResponse(String storeId, long storeVersion, Object itemId, Object newItemValue) {
        super("updateStoreResponse", storeId, storeVersion);
        this.itemId = itemId;
        this.newItemValue = newItemValue;
    }

}
