/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

import java.util.Map;

public class StoreContentResponse extends BaseStoreResponse {

    /**
     * Store items.
     */
    public final Map items;

    public StoreContentResponse(String storeId, StoreContent content) {
        super("storeContentResponse", storeId, content.storeVersion);
        this.items = content.items;
    }
}
