/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

import io.reactivex.disposables.Disposable;

public abstract class BaseStoreStream {

   protected Disposable subscription;

   protected boolean isSubscribed() {
      return subscription != null && !subscription.isDisposed();
   }

   protected void assertNotSubscribed() {
      if (isSubscribed()) {
         throw new IllegalArgumentException("Stream already subscribed!");
      }
   }

   public void unsubscribe() {
      if (isSubscribed()) {
         this.subscription.dispose();
      }
   }
}
