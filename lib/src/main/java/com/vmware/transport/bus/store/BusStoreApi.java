/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store;

import com.vmware.transport.bus.store.model.BusStore;
import com.vmware.transport.bus.store.model.StoreReadyResult;

/**
 * BusStoreApi provide interface for interaction with BusStores.
 */
public interface BusStoreApi {

   /**
    * Create a new Store, if the store already exists, then it will be returned, safe for async operations.
    * @param storeType, the string ID of the store you want to create (i.e. "UserStore")
    * @return reference to the BusStore you have just created.
    */
    <K, T> BusStore<K, T> createStore(String storeType);

   /**
    * Get a reference to the existing store. If the store does not exist, nothing will be returned.
    * @param storeType the string ID of the store you want a reference to (i.e. "UserStore")
    */
    <K, T> BusStore<K, T> getStore(String storeType);

   /**
    * Removes a store from the stores collection but doesn't reset its content.
    * @param storeType, the string ID of the store you want to destroy (i.e. "UserStore")
    * @return true if the store was deleted.
    */
   boolean destroyStore(String storeType);

   /**
    * When you need to wait for more than a single store to be ready, readyJoin takes an array of storeTypes
    * and returns a reference to StoreReadyResult, any function you pass to whenReady will be executed once all
    * stores have been initialized.
    * @param {Array<StoreType>} caches array of StoreTypes you want to wait for initialization on.
    */
   StoreReadyResult readyJoin(String... storeTypes);

   /**
    * Wipe out everything, will eradicate all state from all stores by destroying all stores.
    */
   void wipeAllStores();
}
