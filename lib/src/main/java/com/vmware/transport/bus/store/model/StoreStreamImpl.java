/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

import io.reactivex.Observable;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

public class StoreStreamImpl<T> extends BaseStoreStream implements StoreStream<T> {

   private final Observable<StoreStateChange<?, T, ?>> stream;

   public StoreStreamImpl(Observable<StoreStateChange<?, T, ?>> stream) {
      this.stream = stream;
   }

   @Override
   public void subscribe(Consumer<T> handler) {
      if (handler == null) {
         throw new IllegalArgumentException("Invalid store stream handler.");
      }
      assertNotSubscribed();

      this.subscription = this.stream.subscribe(
            (StoreStateChange<?, T, ?> item) -> handler.accept(item.getValue()));
   }

   @Override
   public void subscribe(BiConsumer<T, StoreStateChange<?, T, ?>> handler) {
      if (handler == null) {
         throw new IllegalArgumentException("Invalid store stream handler.");
      }
      assertNotSubscribed();

      this.subscription = this.stream.subscribe(
            (StoreStateChange<?, T, ?> item) -> handler.accept(item.getValue(), item));
   }
}
