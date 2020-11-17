/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

import lombok.Getter;

public class BaseStoreState<T, V> {

   @Getter
   private final T type;

   @Getter
   private final V value;

   public BaseStoreState(T type, V value) {
      this.type = type;
      this.value = value;
   }
}
