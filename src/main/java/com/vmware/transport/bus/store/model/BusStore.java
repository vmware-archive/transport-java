/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

import java.util.List;
import java.util.Map;

import io.reactivex.functions.Consumer;

public interface BusStore<K, T> {

   /**
    * Return the ID of the store.
    */
   String getStoreType();

   /**
    * Place an object into the store, will broadcast to all subscribers online for state changes.
    * @param id, the id of your object.
    * @param value, the object to be added ot the store
    * @param state, the state change event you want to broadcast with this action
    *               (created, updated etc).
    */
   <State> void put(K id, T value, State state);

   /**
    * Retrieve an object from the store
    * @param id, the id of the object you wish to get.
    * @return the object you're looking for.
    */
   T get(K id);

   /**
    * Get all values from sotre.
    * @return {@link List<T>} with every item in the store.
    */
   List<T> allValues();

   /**
    * Get the entire store as a map.
    * @return {@link Map<K, T>}
    */
   Map<K, T> allValuesAsMap();

   /**
    * Get the store content at the current time.
    * @return {@link StoreContent <K, T>}
    */
   StoreContent<K, T> getStoreContent();

   /**
    * Remove an object from the store.
    * @param id, the id of the object to be removed.
    * @param state, the state to be sent to subscribers notifying store deletion.
    * @return true if the object was removed, false if not.
    */
    <State> boolean remove(K id , State state);

   /**
    * Send a mutation command to any subscribers handling mutations.
    * @param mutationRequest, mutation request describing what store value(s) that should be mutated.
    * @param mutationType, the type of the mutation.
    * @param successHandler, handler which will be invoked if the mutation operation was successful.
    * @param errorHandler, handler which will be invoked if the mutation operation fails.
    * @return true if mutation command was placed in stream
    */
   <MutationRequestType, MutationType> boolean mutate(
         MutationRequestType mutationRequest, MutationType mutationType,
         Consumer<Object> successHandler, Consumer<Object> errorHandler);

   /**
    * Populate the store with a collection of objects and their ID's.
    * @param items, a Map of your ids mapped to your Objects.
    * @return false if the store has already been populated (has objects).
    */
   boolean populate(Map<K, T> items);

   /**
    * Returns {@link BusStoreInitializer} instance which can be used to add
    * the initial items to the store. The BusStoreInitializer can be used as an
    * alternative to the populate() API.
    * @return null if the store was already initialized.
    */
   BusStoreInitializer<K, T> getBusStoreInitializer();

   /**
    * Subscribe to state changes for a specific object.
    * @param id, the id of the object you wish to receive updates.
    * @param stateChangeType, optional state change types you wish to listen to
    * @return {@link StoreStream<T>} stream that will tick the object you're online for.
    */
   <State> StoreStream<T> onChange(K id, State... stateChangeType);

   /**
    * Subscribe to state changes for all objects in the store.
    * @param stateChangeType, optional state change types you wish to listen to
    * @return {@link StoreStream<T>} stream that will tick the object you're online for.
    */
   <State> StoreStream<T> onAllChanges(State... stateChangeType);

   /**
    * Subscribe to mutation requests via mutate()
    * @param mutationType, optional mutation types
    * @return stream that will tick mutation requests you're online for.
    */
   <MutationType, MutationRequestType> MutateStream<MutationRequestType>
         onMutationRequest(MutationType... mutationType);

   /**
    * Notify when the store has been initialized (via populate() or initialize(), etc.)
    * @param readyFunction, handler that accepts the entire store as a map.
    */
   void whenReady(Consumer<Map<K, T>> readyFunction);

   /**
    * Flip an internal bit to set the store to ready, notify all watchers.
    */
   void initialize();

   /**
    * Return true if the store is already initialized.
    */
   boolean isInitialized();

   /**
    * Will wipe all data out, in case you need a clean state.
    */
   void reset();

   /**
    * Returns the current version of the store. Each item change increases the
    * store's version with 1.
    */
   long getCurrentVersion();

   /**
    * The type of the store's values. The type will be used at runtime
    * to deserialize item values coming from the UI. Should be set for
    * galactic stores with non-string values.
    * @param valueType
    */
   void setValueType(Class<T> valueType);

   /**
    * Returns the class type of the store values.
    */
   Class<T> getValueType();

   /**
    * The type of the store's keys. The type will be used at runtime
    * to deserialize item keys coming from the UI. Should be set for
    * galactic stores with non-string keys.
    * @param keyType
    */
   void setKeyType(Class<K> keyType);

   /**
    * Returns the class type of the store keys.
    */
   Class<K> getKeyType();
}
