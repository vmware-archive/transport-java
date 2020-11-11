/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
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
