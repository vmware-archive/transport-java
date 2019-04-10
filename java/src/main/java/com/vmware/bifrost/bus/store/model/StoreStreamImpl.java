/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class StoreStreamImpl<T> extends BaseStoreStream implements StoreStream<T> {

   private final Observable<T> stream;

   public StoreStreamImpl(Observable<T> stream) {
      this.stream = stream;
   }

   @Override
   public void subscribe(Consumer<T> handler) {
      if (handler == null) {
         throw new IllegalArgumentException("Invalid store stream handler.");
      }
      assertNotSubscribed();

      this.subscription = this.stream.subscribe((T item) -> handler.accept(item));
   }
}
