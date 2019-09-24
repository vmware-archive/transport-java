/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

import java.util.UUID;

public class TestStoreItem {

   public String name;

   public int count = 0;

   public UUID uuid;

   public TestStoreItem() {

   }

   public TestStoreItem(String name, int count) {
      this.uuid = UUID.randomUUID();
      this.name = name;
      this.count = count;
   }
}
