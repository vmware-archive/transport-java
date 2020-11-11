/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;

/**
 * StoreStream wraps an Observable, allowing for future underlying logic manipulation without
 * worrying about breaking API's.
 */
public interface StoreStream<T> {

   /**
    * Subscribe to Observable stream.
    * @param handler, a Consumer function to handle ticks on stream.
    */
   void subscribe(Consumer<T> handler);

   /**
    * Subscribe to Observable stream.
    * @param handler, a BiConsumer function that accepts the changed item as first
    *                 argument and the StoreStateChange as a second.
    */
   void subscribe(BiConsumer<T, StoreStateChange<?, T, ?>> handler);

   /**
    * Unsubscribe from the Store stream.
    */
   void unsubscribe();
}
