/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

public class UpdateStoreRequest {

    public String storeId;

    public long clientStoreVersion;

    public Object itemId;

    public Object newItemValue;
}
