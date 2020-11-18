/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
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
