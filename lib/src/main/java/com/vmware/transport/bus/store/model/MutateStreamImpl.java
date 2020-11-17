/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class MutateStreamImpl<T> extends BaseStoreStream implements MutateStream<T> {

   private final Observable<MutationRequestWrapper<T>> stream;

   public MutateStreamImpl(Observable<MutationRequestWrapper<T>> stream) {
      this.stream = stream;
   }

   @Override
   public void subscribe(Consumer<MutationRequestWrapper<T>> handler) {
      if (handler == null) {
         throw new IllegalArgumentException("Invalid mutate stream handler.");
      }
      assertNotSubscribed();

      this.subscription = this.stream.subscribe(
            (MutationRequestWrapper<T> request) -> handler.accept(request));
   }
}
