/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.util.Loggable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.functions.Consumer;
import io.reactivex.Observable;
import org.apache.commons.lang3.ArrayUtils;

public class BusStoreImpl<T> extends Loggable implements BusStore<T> {

   private final UUID uuid;
   private final String storeType;
   private final EventBus eventBus;
   private final Map<UUID, T> cache;

   private final String cacheStreamChannelName;
   private final String cacheMutationChannelName;
   private final String cacheReadyChannelName;

   private final AtomicBoolean isCacheInitialized = new AtomicBoolean(false);

   public BusStoreImpl(EventBus eventBus, String storeType) {
      this.eventBus = eventBus;
      this.uuid = UUID.randomUUID();
      this.storeType = storeType;
      this.cache = new ConcurrentHashMap<>();

      this.cacheStreamChannelName = "stores::store-change-" + this.uuid + "-" + this.storeType;
      this.cacheMutationChannelName = "stores::store-mutation-" + this.uuid + "-" + this.storeType;
      this.cacheReadyChannelName = "stores::store-ready-" + this.uuid + "-" + this.storeType;

      infoMsg(String.format("Store: New Store [%s] was created with id %s, named %s",
            this.storeType, this.uuid, this.storeType));
   }

   public boolean isInitialized() {
      return this.isCacheInitialized.get();
   }

   @Override
   public <State> void put(UUID id, T value, State state) {
      if (id == null) {
         return;
      }
      this.cache.put(id, value);
      this.sendChangeBroadcast(state, id, value);
      this.logDebugMessage(String.format("Store: [%s] added new object with id: %s", storeType, id));
   }

   @Override
   public T get(UUID id) {
      if (id  == null) {
         return null;
      }
      return cache.get(id);
   }

   @Override
   public List<T> allValues() {
      return new ArrayList<>(cache.values());
   }

   @Override
   public Map<UUID, T> allValuesAsMap() {
      return new HashMap<>(cache);
   }

   @Override
   public <State> boolean remove(UUID id, State state) {
      if (id == null) {
         return false;
      }
      T obj = this.cache.remove(id);
      if (obj != null) {
         this.sendChangeBroadcast(state, id, obj);
         this.eventBus.getApi().complete(getObjectChannelName(id), this.storeType);
         this.logDebugMessage(String.format(" Store: [%s] Remove object with id %s", this.storeType, id.toString()));
         return true;
      }
      return false;
   }

   @Override
   public <V, MutationType> boolean mutate(V value, MutationType mutationType,
         Consumer<Object> successHandler, Consumer<Object> errorHandler) {

      final StoreStateMutation<MutationType, V> mutation =
            new StoreStateMutation<>(mutationType, value);
      mutation.setSuccessHandler(successHandler);
      mutation.setErrorHandler(errorHandler);

      this.eventBus.sendRequestMessage(this.cacheMutationChannelName, mutation);

      this.logDebugMessage(String.format("Store: [%s] fired mutation operation", this.storeType));
      return true;
   }

   @Override
   public <MutationType, MutationRequestType> MutateStream<MutationRequestType>
         onMutationRequest(MutationType... mutationType) {

      final Observable<StoreStateMutation<MutationType, MutationRequestType>> stream =
         this.eventBus.getApi().getChannel(this.cacheMutationChannelName, getName()).map(
               (Message msg) -> (StoreStateMutation<MutationType, MutationRequestType>) msg.getPayload());

      final Observable<MutationRequestWrapper<MutationRequestType>> filterStream =
            stream.filter((StoreStateMutation<MutationType, MutationRequestType> mutation) ->
               ArrayUtils.isEmpty(mutationType) ||
                     ArrayUtils.indexOf(mutationType, mutation.getType()) >= 0
            ).map((StoreStateMutation<MutationType, MutationRequestType> mutation) ->
                new MutationRequestWrapper<>(
                     mutation.getValue(),
                     mutation.getSuccessHandler(),
                     mutation.getErrorHandler())
            );

      return new MutateStreamImpl<>(filterStream);
   }

   @Override
   public synchronized boolean populate(Map<UUID, T> items) {
      if (this.cache.isEmpty() && !this.isInitialized()) {
         for (Map.Entry<UUID, T> item : items.entrySet()) {
            this.cache.put(item.getKey(), item.getValue());
         }

         this.initialize();
         return true;
      }
      return false;
   }

   @Override
   public BusStoreInitializer<T> getBusStoreInitializer() {
      if (isCacheInitialized.get()) {
         return null;
      }

      return new BusStoreInitializer<T>() {
         @Override
         public BusStoreInitializer<T> add(UUID id, T value) {
            if (id != null) {
               cache.put(id, value);
            }
            return this;
         }

         @Override
         public void done() {
            initialize();
         }
      };
   }

   @Override
   public <State> StoreStream<T> onChange(UUID id, State... stateChangeType) {
      if (id == null) {
         return null;
      }

      final Observable<Message> cacheStreamChannel =
            this.eventBus.getApi().getResponseChannel(getObjectChannelName(id), getName());
      final Observable<Message> cacheErrorChannel =
            this.eventBus.getApi().getErrorChannel(getObjectChannelName(id), getName());

      final Observable<StoreStateChange<State, T>> stream =
            Observable.merge(cacheStreamChannel, cacheErrorChannel)
                  .map( (Message msg) -> (StoreStateChange<State, T>) msg.getPayload());

      return new StoreStreamImpl<>(filterByChangeType(stream, stateChangeType));
   }

   @Override
   public <State> StoreStream<T> onAllChanges(State... stateChangeType) {
      final Observable<Message> cacheStreamChannel =
            this.eventBus.getApi().getResponseChannel(this.cacheStreamChannelName, getName());
      final Observable<Message> cacheErrorChannel =
            this.eventBus.getApi().getErrorChannel(this.cacheStreamChannelName, getName());

      final Observable<StoreStateChange<State, T>> stream =
            Observable.merge(cacheStreamChannel, cacheErrorChannel)
                  .map( (Message msg) -> (StoreStateChange<State, T>) msg.getPayload());

      return new StoreStreamImpl<>(filterByChangeType(stream, stateChangeType));
   }

   @Override
   public synchronized void whenReady(Consumer<Map<UUID, T>> readyFunction) {
      if (this.isCacheInitialized.get()) {
         this.logDebugMessage(String.format("Store: [%s] Ready! Contains %d values",
               this.storeType, this.cache.size()));
         try {
            readyFunction.accept(this.allValuesAsMap());
         } catch(Exception ex) {
            this.logErrorMessage("Error in whenReady handler.", ex.getMessage());
         }
      } else {
         this.eventBus.listenOnce(this.cacheReadyChannelName, (Message message) -> {
            readyFunction.accept((Map<UUID, T>) message.getPayload());
         });
      }
   }

   @Override
   public synchronized void initialize() {
      if (!this.isCacheInitialized.getAndSet(true)) {
         infoMsg(String.format("Store: [%s] Initialized!", this.storeType));
         sendResponseMessage(this.cacheReadyChannelName, this.allValuesAsMap());
      }
   }

   @Override
   public synchronized void reset() {
      this.cache.clear();
      this.isCacheInitialized.set(false);
      infoMsg(String.format("Store: [%s] has been reset. All data wiped", this.storeType));
   }

   private <State> Observable<T> filterByChangeType(
         Observable<StoreStateChange<State, T>> stream, State... stateChangeType) {

      return stream.filter((StoreStateChange<State, T> state) ->
            ArrayUtils.isEmpty(stateChangeType) ||
                  ArrayUtils.indexOf(stateChangeType, state.getType()) >= 0
      ).map((StoreStateChange<State, T> state) -> state.getValue());
   }

   private <C> void sendChangeBroadcast(C changeType, UUID id, T value) {

      final StoreStateChange<C, T> stateChange = new StoreStateChange<>(id, changeType, value);

      sendResponseMessage(this.cacheStreamChannelName, stateChange);
      sendResponseMessage(this.getObjectChannelName(stateChange.getObjectId()), stateChange);
   }

   private void sendResponseMessage(String channel, Object payload) {
      // Don't send the response message if there are no listeners.
      if (this.eventBus.getApi().getChannelRefCount(channel) > 0) {
         this.eventBus.sendResponseMessage(channel, payload);
      }
   }

   private String getObjectChannelName(UUID objectId) {
      return "store-" + this.uuid + "-object-" + objectId;
   }

   private void infoMsg(String msg) {
      this.logInfoMessage("\uD83D\uDDC4", getName(), msg);
   }
}
