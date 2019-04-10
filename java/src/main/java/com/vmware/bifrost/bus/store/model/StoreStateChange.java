/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

import lombok.Getter;

import java.util.UUID;

public class StoreStateChange<T, V> extends BaseStoreState<T,V > {

   @Getter
   private final UUID objectId;

   public StoreStateChange(UUID objectId, T changeType, V objectValue) {
      super(changeType, objectValue);
      this.objectId = objectId;
   }

}
