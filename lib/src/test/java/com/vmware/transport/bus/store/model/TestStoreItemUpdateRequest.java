/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
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
