/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

import io.reactivex.functions.Consumer;
import lombok.Getter;
import lombok.Setter;

public class StoreStateMutation<T, V> extends BaseStoreState<T, V> {

   @Getter
   @Setter
   private Consumer<Object> errorHandler;

   @Getter
   @Setter
   private Consumer<Object> successHandler;

   public StoreStateMutation(T changeType, V objectValue) {
      super(changeType, objectValue);
   }
}
