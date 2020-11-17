/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

/**
 * Helper initializer class which can be used to populate the initial content
 * of a given store. It can be used as an alternative of the BusStore.populate() method.
 */
public interface BusStoreInitializer<K, T> {

   /**
    * Adds a new item to store.
    */
   BusStoreInitializer<K, T> add(K id, T value);

   /**
    * Should be called when all initial items are added to the store.
    * It will call the BusStore.initialize() method.
    */
   void done();
}
