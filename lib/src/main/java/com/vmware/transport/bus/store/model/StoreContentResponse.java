/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
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
