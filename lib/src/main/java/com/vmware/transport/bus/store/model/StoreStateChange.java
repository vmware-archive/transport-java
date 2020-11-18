/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

import lombok.Getter;
import lombok.Setter;

public class StoreStateChange<T, V, K> extends BaseStoreState<T,V> {

   @Getter
   private final K objectId;

   @Getter
   private final long storeVersion;

   @Getter
   private final boolean isDeleteChange;

   public StoreStateChange(K objectId, T changeType, V objectValue, long storeVersion, boolean isDeleteChange) {
      super(changeType, objectValue);
      this.objectId = objectId;
      this.storeVersion = storeVersion;
      this.isDeleteChange = isDeleteChange;
   }

}
