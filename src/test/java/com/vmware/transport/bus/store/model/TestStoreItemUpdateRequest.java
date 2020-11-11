/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

public class TestStoreItemUpdateRequest {

   public TestStoreItem item;

   public int incCountBy;

   TestStoreItemUpdateRequest(TestStoreItem item, int incCountBy) {
      this.item = item;
      this.incCountBy = incCountBy;
   }
}
