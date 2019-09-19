/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

public class UpdateStoreRequest {

    public String storeId;

    public long clientStoreVersion;

    public Object itemId;

    public Object newItemValue;
}
