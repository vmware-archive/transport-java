/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

import lombok.Getter;

public class StoreStateChange<T, V, K> extends BaseStoreState<T,V> {

   @Getter
   private final K objectId;

   public StoreStateChange(K objectId, T changeType, V objectValue) {
      super(changeType, objectValue);
      this.objectId = objectId;
   }

}
