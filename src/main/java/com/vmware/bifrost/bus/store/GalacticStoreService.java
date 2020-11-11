/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bus.model.MonitorObject;
import com.vmware.bifrost.bus.model.MonitorType;
import com.vmware.bifrost.bus.store.model.BusStore;
import com.vmware.bifrost.bus.store.model.BusStoreError;
import com.vmware.bifrost.bus.store.model.CloseStoreRequest;
import com.vmware.bifrost.bus.store.model.OpenStoreRequest;
import com.vmware.bifrost.bus.store.model.StoreContentResponse;
import com.vmware.bifrost.bus.store.model.StoreStream;
import com.vmware.bifrost.bus.store.model.UpdateStoreResponse;
import com.vmware.bifrost.bus.store.model.UpdateStoreRequest;
import com.vmware.bifrost.core.AbstractBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class GalacticStoreService extends AbstractBase {

    public static final String GALACTIC_STORE_SYNC_UPDATE = "galacticStoreSyncUpdate";
    public static final String GALACTIC_STORE_SYNC_REMOVE = "galacticStoreSyncRemove";

    private BusStoreApi storeManager;

    private static final String STORE_SYNC_CHANNEL_PREFIX = "fabric-store-sync.";

    private final Map<String, GalacticStoreListener> openedStoresMap = new HashMap<>();
    private final Map<String, Set<String>> clientChannelsToOpenStoresMap = new HashMap<>();
    private final Map<String, BusTransaction> syncChannelsToRequestListeners = new HashMap<>();

    @Autowired
    public GalacticStoreService(BusStoreApi storeManager) {
        this.storeManager = storeManager;
    }

    @Override
    public void initialize() {

        // Monitor the system channel for MonitorNewBridgeSubscription and
        // MonitorCloseChannel events related to store sync channels.
        bus.getApi().getMonitor().subscribe( message -> {
            final MonitorObject mo = (MonitorObject) message.getPayload();

            if (mo == null || mo.getChannel() == null ||
                  !mo.getChannel().startsWith(STORE_SYNC_CHANNEL_PREFIX)) {
                // not a store sync channel, ignore the message
                return;
            }

            final String channelName = mo.getChannel();
            if (mo.getType() == MonitorType.MonitorNewBridgeSubscription) {
                openNewStoreSyncChannel(channelName);
            } else if (mo.getType() == MonitorType.MonitorCloseChannel) {
                closeStoreSyncChannel(channelName);
            }
        });
    }

    private void openNewStoreSyncChannel(String channelName) {
        synchronized (clientChannelsToOpenStoresMap) {

            if (clientChannelsToOpenStoresMap.containsKey(channelName)) {
                // channel already opened.
                return;
            }

            clientChannelsToOpenStoresMap.put(channelName, new HashSet<>());

            syncChannelsToRequestListeners.put(channelName,
                  bus.listenRequestStream(channelName, message -> {
                      try {
                          final Request request = (Request) message.getPayload();
                          switch (request.getRequest()) {
                              case GalacticStoreCommands.OpenStore:
                                  onOpenStoreRequest(channelName, request);
                                  break;
                              case GalacticStoreCommands.CloseStore:
                                  onCloseStoreRequest(channelName, request);
                                  break;
                              case GalacticStoreCommands.UpdateStore:
                                  onUpdateStoreRequest(channelName, request);
                          }

                      } catch (Exception ex) {
                          logErrorMessage("Failed to process request",
                                ex.getMessage() != null ? ex.getMessage() : ex.toString());
                      }
                  }));
        }
    }

    private void onUpdateStoreRequest(String syncChannelName, Request request) {
        UpdateStoreRequest updateStoreRequest;

        try {
            updateStoreRequest = mapper.convertValue(request.getPayload(), UpdateStoreRequest.class);
        } catch (Exception ex) {
            logErrorMessage("Invalid UpdateStoreRequest", ex.getMessage());
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid UpdateStoreRequest: " + ex.getMessage()), request.getId());
            return;
        }

        if (updateStoreRequest == null) {
            logErrorMessage("Invalid UpdateStoreRequest", "null request");
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid UpdateStoreRequest: null request"), request.getId());
            return;
        }

        if (updateStoreRequest.storeId == null) {
            logErrorMessage("Invalid UpdateStoreRequest", "null storeId");
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid UpdateStoreRequest: null storeId"), request.getId());
            return;
        }

        BusStore store = storeManager.getStore(updateStoreRequest.storeId);
        if (store == null) {
            logErrorMessage("Invalid updateStore request: requested store doesn't exist", updateStoreRequest.storeId);
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Cannot update non-existing store: " + updateStoreRequest.storeId,
                        updateStoreRequest.storeId),
                  request.getId());
            return;
        }

        Object itemId;
        Object itemValue;
        try {
            if (store.getKeyType() != null) {
                itemId = mapper.convertValue(updateStoreRequest.itemId, store.getKeyType());
            } else {
                itemId = updateStoreRequest.itemId;
            }

            if (updateStoreRequest.newItemValue != null && store.getValueType() != null) {
                itemValue = mapper.convertValue(updateStoreRequest.newItemValue, store.getValueType());
            } else {
                itemValue = updateStoreRequest.newItemValue;
            }
        } catch (Exception ex) {
            logErrorMessage("Invalid UpdateStoreRequest.", ex.getMessage());
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid UpdateStoreRequest: " + ex.getMessage(),
                        updateStoreRequest.storeId, updateStoreRequest.itemId),
                  request.getId());
            return;
        }

        if (itemValue != null) {
            store.put(itemId, itemValue, GALACTIC_STORE_SYNC_UPDATE);
        } else {
            store.remove(itemId, GALACTIC_STORE_SYNC_REMOVE);
        }
    }

    private void onOpenStoreRequest(String syncChannelName, Request request) {
        OpenStoreRequest openStoreRequest;
        try {
            openStoreRequest = mapper.convertValue(request.getPayload(), OpenStoreRequest.class);
        } catch (Exception ex) {
            logErrorMessage("Invalid OpenStoreRequest.", ex.getMessage());
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid OpenStoreRequest: " + ex.getMessage()), request.getId());
            return;
        }

        if (openStoreRequest == null) {
            logErrorMessage("Invalid OpenStoreRequest", "null request");
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid OpenStoreRequest: null request"), request.getId());
            return;
        }

        if (openStoreRequest.storeId == null) {
            logErrorMessage("Invalid OpenStoreRequest", "null storeId");
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid OpenStoreRequest: null storeId"), request.getId());
            return;
        }

        BusStore store;
        synchronized (clientChannelsToOpenStoresMap) {
            store = storeManager.getStore(openStoreRequest.storeId);
            if (store == null) {
                logErrorMessage("Invalid openStore request: requested store doesn't exist", openStoreRequest.storeId);
                bus.sendErrorMessageWithId(syncChannelName,
                      new BusStoreError("Cannot open non-existing store: " + openStoreRequest.storeId,
                            openStoreRequest.storeId),
                      request.getId());
                return;
            }

            if (clientChannelsToOpenStoresMap.containsKey(syncChannelName)) {
                clientChannelsToOpenStoresMap.get(syncChannelName).add(openStoreRequest.storeId);
            }
            if (!openedStoresMap.containsKey(openStoreRequest.storeId)) {
                openedStoresMap.put(openStoreRequest.storeId, new GalacticStoreListener(store));
            }
            openedStoresMap.get(openStoreRequest.storeId).addClientChannel(syncChannelName);
        }

        // Wait the store to be initialized and send the store content to the client.
        store.whenReady(map -> {
            bus.sendResponseMessageWithId(syncChannelName,
                  new StoreContentResponse(openStoreRequest.storeId, store.getStoreContent()),
                  request.getId());
        });
    }

    private void onCloseStoreRequest(String syncChannelName, Request request) {
        CloseStoreRequest closeStoreRequest;
        try {
            closeStoreRequest = mapper.convertValue(request.getPayload(), CloseStoreRequest.class);
        } catch (Exception ex) {
            logErrorMessage("Invalid CloseStoreRequest.", ex.getMessage());
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid CloseStoreRequest: " + ex.getMessage()), request.getId());
            return;
        }

        if (closeStoreRequest == null) {
            logErrorMessage("Invalid CloseStoreRequest", "null request");
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid CloseStoreRequest: null request"), request.getId());
            return;
        }

        if (closeStoreRequest.storeId == null) {
            logErrorMessage("Invalid CloseStoreRequest", "null storeId");
            bus.sendErrorMessageWithId(syncChannelName,
                  new BusStoreError("Invalid CloseStoreRequest: null storeId"), request.getId());
            return;
        }

        synchronized (clientChannelsToOpenStoresMap) {
            if (clientChannelsToOpenStoresMap.containsKey(syncChannelName)) {
                clientChannelsToOpenStoresMap.get(syncChannelName).remove(closeStoreRequest.storeId);
            }

            GalacticStoreListener listener = openedStoresMap.get(closeStoreRequest.storeId);
            if (listener != null && !listener.isEmpty()) {
                listener.removeClientChannel(syncChannelName);
                if (listener.isEmpty()) {
                    listener.unsubscribe();
                    openedStoresMap.remove(closeStoreRequest.storeId);
                }
            }
        }
    }

    private void closeStoreSyncChannel(String channelName) {
        BusTransaction requestListener;
        synchronized (clientChannelsToOpenStoresMap) {
            if (!clientChannelsToOpenStoresMap.containsKey(channelName)) {
                // channel already closed.
                return;
            }

            for (String store : clientChannelsToOpenStoresMap.get(channelName)) {
                GalacticStoreListener listener = openedStoresMap.get(store);
                if (listener != null) {
                    listener.removeClientChannel(channelName);
                    if (listener.isEmpty()) {
                        listener.unsubscribe();
                        openedStoresMap.remove(store);
                    }
                }
            }
            clientChannelsToOpenStoresMap.remove(channelName);
            bus.closeChannel(channelName, this.getName());
            requestListener = syncChannelsToRequestListeners.remove(channelName);
        }
        if (requestListener != null) {
            requestListener.unsubscribe();
        }
    }

    private class GalacticStoreListener {

        private final Set<String> clientChannels = new HashSet<>();

        private final StoreStream<?> storeStream;

        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

        GalacticStoreListener(final BusStore store) {
            this.storeStream = store.onAllChanges();
            this.storeStream.subscribe((item, stateChange) -> {

                UpdateStoreResponse updateStoreMsg = new UpdateStoreResponse(
                      store.getStoreType(),
                      stateChange.getStoreVersion(),
                      stateChange.getObjectId(),
                      stateChange.isDeleteChange() ? null : stateChange.getValue());

                try {
                    readWriteLock.readLock().lock();
                    for (String channel : clientChannels) {
                        try {
                            bus.sendResponseMessage(channel, updateStoreMsg);
                        } catch (Exception ex) {
                            logErrorMessage("Failed to send store update", ex.getMessage());
                        }
                    }
                } finally {
                    readWriteLock.readLock().unlock();
                }
            });
        }

        void addClientChannel(String clientChannel) {
            this.readWriteLock.writeLock().lock();
            try {
                clientChannels.add(clientChannel);
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }

        void removeClientChannel(String clientChannel) {
            this.readWriteLock.writeLock().lock();
            try {
                clientChannels.remove(clientChannel);
            } finally {
                this.readWriteLock.writeLock().unlock();
            }
        }

        public boolean isEmpty() {
            try {
                this.readWriteLock.readLock().lock();
                return this.clientChannels.isEmpty();
            } finally {
                this.readWriteLock.readLock().unlock();
            }
        }

        public void unsubscribe() {
            this.storeStream.unsubscribe();
        }
    }

    static class GalacticStoreCommands {
        final static String OpenStore = "openStore";
        final static String UpdateStore = "updateStore";
        final static String CloseStore = "closeStore";

        private GalacticStoreCommands() {}
    }
}
