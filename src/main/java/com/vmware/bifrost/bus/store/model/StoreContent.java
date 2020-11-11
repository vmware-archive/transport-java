/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

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
