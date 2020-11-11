/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bus.store.model;

import com.vmware.transport.bus.EventBus;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.core.util.Loggable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.functions.Consumer;
import io.reactivex.Observable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

public class BusStoreImpl<K, T> extends Loggable implements BusStore<K, T> {

   private final UUID uuid;
   private final String storeType;
   private final EventBus eventBus;
   private final Map<K, T> cache;

   private final String cacheStreamChannelName;
   private final String cacheMutationChannelName;
   private final String cacheReadyChannelName;

   private final AtomicBoolean isCacheInitialized = new AtomicBoolean(false);

   private final AtomicLong storeVersion = new AtomicLong(0);

   @Getter @Setter
   private Class<T> valueType;

   @Getter @Setter
   private Class<K> keyType;

   public BusStoreImpl(EventBus eventBus, String storeType) {
      this.eventBus = eventBus;
      this.uuid = UUID.randomUUID();
      this.storeType = storeType;
      this.cache = new ConcurrentHashMap<>();
      this.cacheStreamChannelName = "stores__store-change-" + this.uuid + "-" + this.storeType;
      this.cacheMutationChannelName = "stores__store-mutation-" + this.uuid + "-" + this.storeType;
      this.cacheReadyChannelName = "stores__store-ready-" + this.uuid + "-" + this.storeType;

      infoMsg(String.format("Store: New Store [%s] was created with id %s, named %s",
            this.storeType, this.uuid, this.storeType));
   }

   public boolean isInitialized() {
      return this.isCacheInitialized.get();
   }

   @Override
   public String getStoreType() {
      return storeType;
   }

   @Override
   public <State> void put(K id, T value, State state) {
      if (id == null) {
         return;
      }
      long version;
      synchronized (this.cache) {
         this.cache.put(id, value);
         version = this.storeVersion.incrementAndGet();
      }
      this.sendChangeBroadcast(state, id, value, version, false);
      this.logDebugMessage(String.format("Store: [%s] added new object with id: %s", storeType, id));
   }

   @Override
   public T get(K id) {
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
   public Map<K, T> allValuesAsMap() {
      return new HashMap<>(cache);
   }

   @Override
   public StoreContent<K, T> getStoreContent() {
      synchronized (this.cache) {
         return new StoreContent<>(this.getCurrentVersion(), allValuesAsMap());
      }
   }

   @Override
   public <State> boolean remove(K id, State state) {
      if (id == null) {
         return false;
      }
      T obj;
      long version = 0;
      synchronized (this.cache) {
         obj = this.cache.remove(id);
         if (obj != null) {
            version = this.storeVersion.incrementAndGet();
         }
      }
      if (obj != null) {
         this.sendChangeBroadcast(state, id, obj, version, true);
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
   public synchronized boolean populate(Map<K, T> items) {
      if (this.cache.isEmpty() && !this.isInitialized()) {
         for (Map.Entry<K, T> item : items.entrySet()) {
            this.cache.put(item.getKey(), item.getValue());
         }

         this.initialize();
         return true;
      }
      return false;
   }

   @Override
   public BusStoreInitializer<K, T> getBusStoreInitializer() {
      if (isCacheInitialized.get()) {
         return null;
      }

      return new BusStoreInitializer<K, T>() {
         @Override
         public BusStoreInitializer<K, T> add(K id, T value) {
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
   public <State> StoreStream<T> onChange(K id, State... stateChangeType) {
      if (id == null) {
         return null;
      }

      final Observable<Message> cacheStreamChannel =
            this.eventBus.getApi().getResponseChannel(getObjectChannelName(id), getName());
      final Observable<Message> cacheErrorChannel =
            this.eventBus.getApi().getErrorChannel(getObjectChannelName(id), getName());

      final Observable<StoreStateChange<?, T, ?>> stream =
            Observable.merge(cacheStreamChannel, cacheErrorChannel)
                  .map( (Message msg) -> (StoreStateChange<?, T, K>) msg.getPayload());

      return new StoreStreamImpl<>(filterByChangeType(stream, stateChangeType));
   }

   @Override
   public <State> StoreStream<T> onAllChanges(State... stateChangeType) {
      final Observable<Message> cacheStreamChannel =
            this.eventBus.getApi().getResponseChannel(this.cacheStreamChannelName, getName());
      final Observable<Message> cacheErrorChannel =
            this.eventBus.getApi().getErrorChannel(this.cacheStreamChannelName, getName());

      final Observable<StoreStateChange<?, T, ?>> stream =
            Observable.merge(cacheStreamChannel, cacheErrorChannel)
                  .map( (Message msg) -> (StoreStateChange<?, T, K>) msg.getPayload());

      return new StoreStreamImpl<>(filterByChangeType(stream, stateChangeType));
   }

   @Override
   public synchronized void whenReady(Consumer<Map<K, T>> readyFunction) {
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
            readyFunction.accept((Map<K, T>) message.getPayload());
         });
      }
   }

   @Override
   public synchronized void initialize() {
      if (!this.isCacheInitialized.getAndSet(true)) {
         infoMsg(String.format("Store: [%s] Initialized!", this.storeType));
         storeVersion.incrementAndGet();
         sendResponseMessage(this.cacheReadyChannelName, this.allValuesAsMap());
      }
   }

   @Override
   public synchronized void reset() {
      this.cache.clear();
      this.isCacheInitialized.set(false);
      infoMsg(String.format("Store: [%s] has been reset. All data wiped", this.storeType));
   }

   @Override
   public long getCurrentVersion() {
      return storeVersion.get();
   }

   private <State> Observable<StoreStateChange<?, T, ?>> filterByChangeType(
         Observable<StoreStateChange<?, T, ?>> stream, State... stateChangeType) {

      return stream.filter((StoreStateChange<?, T, ?> state) ->
            ArrayUtils.isEmpty(stateChangeType) ||
                  ArrayUtils.indexOf(stateChangeType, state.getType()) >= 0
      );
   }

   private <C> void sendChangeBroadcast(C changeType, K id, T value, long storeVersion, boolean isDeleteChange) {

      final StoreStateChange<C, T, K> stateChange =
            new StoreStateChange<>(id, changeType, value, storeVersion, isDeleteChange);

      sendResponseMessage(this.cacheStreamChannelName, stateChange);
      sendResponseMessage(this.getObjectChannelName(stateChange.getObjectId()), stateChange);
   }

   private void sendResponseMessage(String channel, Object payload) {
      // Don't send the response message if there are no listeners.
      if (this.eventBus.getApi().getChannelRefCount(channel) > 0) {
         this.eventBus.sendResponseMessage(channel, payload);
      }
   }

   private String getObjectChannelName(K objectId) {
      return "store-" + this.uuid + "-object-" + objectId;
   }

   private void infoMsg(String msg) {
      this.logInfoMessage("\uD83D\uDDC4", getName(), msg);
   }
}
