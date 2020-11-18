/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

import java.util.Map;

/**
 * Represents a store's content at a given point in time.
 */
public class StoreContent<K, T> {

   /**
    * The store version when the store content was taken.
    */
   public final long storeVersion;

   /**
    * The store items when the store content was taken.
    */
   public final Map<K, T> items;

   public StoreContent(long version, Map<K, T> items) {
      this.storeVersion = version;
      this.items = items;
   }
}
