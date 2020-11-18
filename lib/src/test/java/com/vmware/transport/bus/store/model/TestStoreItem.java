/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

import java.util.Objects;
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

   @Override
   public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      TestStoreItem that = (TestStoreItem) o;
      return count == that.count &&
            Objects.equals(name, that.name) &&
            Objects.equals(uuid, that.uuid);
   }

   @Override
   public int hashCode() {
      return Objects.hash(name, count, uuid);
   }
}
