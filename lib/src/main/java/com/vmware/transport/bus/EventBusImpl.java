/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus;

import com.vmware.transport.bridge.spring.TransportEnabled;
import com.vmware.transport.bridge.spring.TransportService;
import com.vmware.transport.broker.GalacticChannelConfig;
import com.vmware.transport.broker.GalacticMessageHandler;
import com.vmware.transport.broker.MessageBrokerConnector;
import com.vmware.transport.broker.MessageBrokerSubscription;
import com.vmware.transport.bus.model.Channel;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.model.MessageHeaders;
import com.vmware.transport.bus.model.MessageObject;
import com.vmware.transport.bus.model.MessageObjectHandlerConfig;
import com.vmware.transport.bus.model.MessageType;
import com.vmware.transport.bus.model.MonitorObject;
import com.vmware.transport.bus.model.MonitorType;
import com.vmware.transport.bus.store.BusStoreApi;
import com.vmware.transport.core.util.Loggable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component("eventBusImpl")
@SuppressWarnings("unchecked")
public class EventBusImpl extends Loggable implements EventBus {

    private EventBusLowApi api;
    private UUID id;

    private ConcurrentHashMap<String, MessageBrokerConnector> messageBrokersMap;

    private ConcurrentHashMap<String, GalacticChannelData> galacticChannelsMap;

    @Autowired
    private ApplicationContext context;

    private BusStoreApi storeManager;

    // Use setter injection to avoid circular dependencies.
    @Autowired(required = false)
    public void setStoreManager(BusStoreApi storeManager) { this.storeManager = storeManager; }

    @EventListener
    public void handleContextStarted(ContextRefreshedEvent evt) {
        this.init();
    }

    private Map<String, Channel> channelMap;

    public EventBusImpl() {
        this.channelMap = new HashMap<>();
        this.messageBrokersMap = new ConcurrentHashMap<>();
        this.galacticChannelsMap = new ConcurrentHashMap<>();
        this.id = UUID.randomUUID();
        this.api = new EventBusLowApiImpl(this.channelMap);
        this.api.enableMonitorDump(true);
    }

    @Override
    public UUID getId() {
        return this.id;
    }

    @Override
    public EventBusLowApi getApi() {
        return api;
    }

    public BusStoreApi getStoreManager() {
        return storeManager;
    }

    @Override
    public void sendRequestMessage(String channel, Object payload) {
        this.sendRequestMessage(channel, payload, null);
    }

    @Override
    public void sendRequestMessage(String channel, Object payload, MessageHeaders headers) {
        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendRequestMessageWithId(String channel, Object payload, UUID id) {
        this.sendRequestMessageWithId(channel, payload, id, null);
    }

    @Override
    public void sendRequestMessageWithId(String channel, Object payload, UUID id,
                                         MessageHeaders headers) {
        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setId(id);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendRequestMessageToTarget(String channel, Object payload, UUID id, String targetUser) {
        this.sendRequestMessageToTarget(channel, payload, id, targetUser, null);
    }

    @Override
    public void sendRequestMessageToTarget(String channel, Object payload, UUID id, String targetUser,
                                           MessageHeaders headers) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setTargetUser(targetUser);
        config.setId(id);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendResponseMessage(String channel, Object payload) {
        this.sendResponseMessage(channel, payload, null);
    }

    @Override
    public void sendResponseMessage(String channel, Object payload, MessageHeaders headers) {
        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendResponseMessageWithId(String channel, Object payload, UUID id) {
        this.sendResponseMessageWithId(channel, payload, id, null);
    }

    @Override
    public void sendResponseMessageWithId(String channel, Object payload, UUID id,
                                          MessageHeaders headers) {
        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setId(id);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendResponseMessageToTarget(String channel, Object payload, UUID id, String targetUser) {
        this.sendResponseMessageToTarget(channel, payload, id, targetUser, null);
    }

    @Override
    public void sendResponseMessageToTarget(String channel, Object payload, UUID id, String targetUser,
                                            MessageHeaders headers) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setTargetUser(targetUser);
        config.setId(id);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendErrorMessage(String channel, Object payload) {
        this.sendErrorMessage(channel, payload, null);
    }

    @Override
    public void sendErrorMessage(String channel, Object payload, MessageHeaders headers) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeError, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendErrorMessageWithId(String channel, Object payload, UUID id) {
        this.sendErrorMessageWithId(channel, payload, id, null);
    }

    @Override
    public void sendErrorMessageWithId(String channel, Object payload, UUID id, MessageHeaders headers) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeError, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setId(id);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendErrorMessageToTarget(String channel, Object payload, UUID id, String targetUser) {
        this.sendErrorMessageToTarget(channel, payload, id, targetUser, null);
    }

    @Override
    public void sendErrorMessageToTarget(String channel, Object payload, UUID id, String targetUser,
                                         MessageHeaders headers) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeError, payload);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setTargetUser(targetUser);
        config.setId(id);
        config.setHeaders(headers);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      Consumer<Message> successHandler,
                                      Consumer<Message> errorHandler) {
        return this.requestOnce(sendChannel, payload,
                returnChannel, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      Consumer<Message> successHandler,
                                      Consumer<Message> errorHandler) {
        return this.requestOnce(sendChannel, payload,
                sendChannel, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      Consumer<Message> successHandler) {
        return this.requestOnce(sendChannel, payload,
                returnChannel, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      Consumer<Message> successHandler) {
        return this.requestOnce(sendChannel, payload,
                sendChannel, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      String from,
                                      Consumer<Message> successHandler,
                                      Consumer<Message> errorHandler) {

        return this.requestOnceInternal(null, sendChannel,
                payload, returnChannel, from, successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                            String sendChannel,
                                            Object payload,
                                            Consumer<Message> successHandler) {

        return this.requestOnceWithId(uuid, sendChannel, payload, sendChannel, successHandler);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                            String sendChannel,
                                            Object payload,
                                            Consumer<Message> successHandler,
                                            Consumer<Message> errorHandler) {

        return this.requestOnceWithId(uuid, sendChannel, payload, sendChannel, successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                            String sendChannel,
                                            Object payload,
                                            String returnChannel,
                                            Consumer<Message> successHandler) {

        return this.requestOnceWithId(uuid, sendChannel, payload, returnChannel, successHandler, null);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                            String sendChannel,
                                            Object payload,
                                            String returnChannel,
                                            Consumer<Message> successHandler,
                                            Consumer<Message> errorHandler) {

        return this.requestOnceWithId(uuid, sendChannel, payload,
                returnChannel, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                            String sendChannel,
                                            Object payload,
                                            String returnChannel,
                                            String from,
                                            Consumer<Message> successHandler,
                                            Consumer<Message> errorHandler) {

        return this.requestOnceInternal(uuid, sendChannel, payload,
                returnChannel, from, successHandler, errorHandler);
    }

    private BusTransaction requestOnceInternal(UUID id,
                                               String sendChannel,
                                               Object payload,
                                               String returnChannel,
                                               String from,
                                               Consumer<Message> successHandler,
                                               Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload);

        config.setSingleResponse(true);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);
        config.setId(id);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);
        this.api.send(config.getSendChannel(), config, from);

        BusTransaction transaction = new BusHandlerTransaction(sub, messageHandler);
        return transaction;
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        String from,
                                        Consumer<Message> successHandler,
                                        Consumer<Message> errorHandler) {

        return this.requestStreamInternal(null, sendChannel, payload,
                returnChannel, from, successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        Consumer<Message> successHandler,
                                        Consumer<Message> errorHandler) {
        return this.requestStream(sendChannel, payload,
                returnChannel, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        Consumer<Message> successHandler) {
        return this.requestStream(sendChannel, payload,
                returnChannel, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        Consumer<Message> successHandler) {
        return this.requestStream(sendChannel, payload,
                sendChannel, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              Consumer<Message> successHandler) {

        return this.requestStreamWithId(uuid, sendChannel, payload,
                sendChannel, successHandler);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              String returnChannel,
                                              Consumer<Message> successHandler) {

        return this.requestStreamWithId(uuid, sendChannel, payload,
                returnChannel, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              String returnChannel,
                                              Consumer<Message> successHandler,
                                              Consumer<Message> errorHandler) {

        return this.requestStreamWithId(uuid, sendChannel, payload,
                returnChannel, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              String returnChannel,
                                              String from,
                                              Consumer<Message> successHandler,
                                              Consumer<Message> errorHandler) {

        return this.requestStreamInternal(uuid, sendChannel, payload,
                returnChannel, from, successHandler, errorHandler);
    }


    private BusTransaction requestStreamInternal(UUID uuid,
                                                 String sendChannel,
                                                 Object payload,
                                                 String returnChannel,
                                                 String from,
                                                 Consumer<Message> successHandler,
                                                 Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload);

        config.setSingleResponse(false);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);
        config.setId(uuid);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);
        this.api.send(config.getSendChannel(), config, from);

        BusTransaction transaction = new BusHandlerTransaction(sub, messageHandler);
        return transaction;
    }

    @Override
    public BusTransaction listenRequestStream(String channel,
                                              Consumer<Message> successHandler) {
        return this.listenRequestStream(channel, successHandler, null);
    }

    @Override
    public BusTransaction listenRequestStream(String channel,
                                              Consumer<Message> successHandler,
                                              Consumer<Message> errorHandler) {
        return this.listenRequestStream(channel, successHandler, errorHandler, null);
    }

    @Override
    public BusTransaction listenRequestStream(String channel,
                                              Consumer<Message> successHandler,
                                              Consumer<Message> errorHandler,
                                              UUID id) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, null);

        config.setSingleResponse(false);
        config.setReturnChannel(channel);
        config.setSendChannel(channel);
        config.setId(id);

        MessageHandler messageHandler = this.createMessageHandler(config, true);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);

        return new BusHandlerTransaction(sub, messageHandler);
    }

    @Override
    public BusTransaction listenRequestOnce(String channel,
                                            Consumer<Message> successHandler) {
        return this.listenRequestOnce(channel, successHandler, null);
    }


    @Override
    public BusTransaction listenRequestOnce(String channel,
                                            Consumer<Message> successHandler,
                                            Consumer<Message> errorHandler) {
        return this.listenRequestOnce(channel, successHandler, errorHandler, null);
    }

    @Override
    public BusTransaction listenRequestOnce(String channel,
                                            Consumer<Message> successHandler,
                                            Consumer<Message> errorHandler,
                                            UUID id) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, null);

        config.setSingleResponse(true);
        config.setReturnChannel(channel);
        config.setSendChannel(channel);
        config.setId(id);

        MessageHandler messageHandler = this.createMessageHandler(config, true);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);

        return new BusHandlerTransaction(sub, messageHandler);
    }

    @Override
    public BusTransaction listenStream(String channel,
                                       Consumer<Message> successHandler) {
        return this.listenStream(channel, successHandler, null);
    }

    @Override
    public BusTransaction listenStream(String channel,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler) {

        return this.listenStream(channel, successHandler, errorHandler, null);
    }

    @Override
    public BusTransaction listenStream(String channel,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler,
                                       UUID id) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, null);

        config.setSingleResponse(false);
        config.setReturnChannel(channel);
        config.setSendChannel(channel);
        config.setId(id);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);

        return new BusHandlerTransaction(sub, messageHandler);
    }

    @Override
    public BusTransaction listenOnce(String channel,
                                     Consumer<Message> successHandler) {
        return this.listenOnce(channel, successHandler, null);
    }

    @Override
    public BusTransaction listenOnce(String channel,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, null);

        config.setSingleResponse(true);
        config.setReturnChannel(channel);
        config.setSendChannel(channel);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);

        return new BusHandlerTransaction(sub, messageHandler);
    }

    @Override
    public BusTransaction respondOnce(String sendChannel,
                                      Function<Message, Object> generateHandler) {
        return this.respondOnce(sendChannel, sendChannel, generateHandler);
    }

    @Override
    public BusTransaction respondOnce(String sendChannel,
                                      String returnChannel,
                                      Function<Message, Object> generateHandler) {

        MessageObjectHandlerConfig config = new MessageObjectHandlerConfig();
        config.setSingleResponse(true);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);

        MessageResponder messageResponder = this.createMessageResponder(config);
        Disposable sub = messageResponder.generate(generateHandler);
        BusTransaction transaction = new BusResponderTransaction(sub, messageResponder);
        return transaction;
    }

    @Override
    public BusTransaction respondStream(String sendChannel,
                                        String returnChannel,
                                        Function<Message, Object> generateHandler) {

        MessageObjectHandlerConfig config = new MessageObjectHandlerConfig();
        config.setSingleResponse(false);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);

        MessageResponder messageResponder = this.createMessageResponder(config);
        Disposable sub = messageResponder.generate(generateHandler);
        BusTransaction transaction = new BusResponderTransaction(sub, messageResponder);
        return transaction;
    }

    @Override
    public BusTransaction respondStream(String sendChannel,
                                        Function<Message, Object> generateHandler) {
        return this.respondStream(sendChannel, sendChannel, generateHandler);
    }

    @Override
    public void closeChannel(String channel, String from) {
        this.api.close(channel, from);
    }

    @Override
    public Transaction createTransaction() {
        return this.createTransaction(Transaction.TransactionType.ASYNC);
    }

    @Override
    public Transaction createTransaction(Transaction.TransactionType type) {
        return this.createTransaction(type, UUID.randomUUID());
    }

    @Override
    public Transaction createTransaction(Transaction.TransactionType type, String name) {
        return new TransactionImpl(this, this.storeManager, type, name);
    }

    @Override
    public Transaction createTransaction(Transaction.TransactionType type, String name, UUID id) {
        return new TransactionImpl(this, this.storeManager, type, name, id);
    }

    @Override
    public Transaction createTransaction(Transaction.TransactionType type, UUID id) {
        return new TransactionImpl(this, this.storeManager, type, null, id);
    }

    @Override
    public boolean registerMessageBroker(MessageBrokerConnector messageBrokerConnector) {
        synchronized (this.messageBrokersMap) {
            if (this.messageBrokersMap.containsKey(messageBrokerConnector.getMessageBrokerId())) {
                return false;
            }
            this.messageBrokersMap.put(messageBrokerConnector.getMessageBrokerId(), messageBrokerConnector);
        }
        messageBrokerConnector.connectMessageBroker();
        return true;
    }

    @Override
    public boolean unregisterMessageBroker(String messageBrokerId) {
        MessageBrokerConnector connector = this.messageBrokersMap.remove(messageBrokerId);
        if (connector != null) {
            connector.disconnectMessageBroker();
            return true;
        }
        return false;
    }

    @Override
    public boolean markChannelAsGalactic(final String channel, final GalacticChannelConfig config) {
        synchronized (this.galacticChannelsMap) {
            if (this.galacticChannelsMap.containsKey(channel)) {
                logWarnMessage("Channel " + channel + " already marked as galactic.");
                return false;
            }
            MessageBrokerConnector messageBroker =
                    this.messageBrokersMap.get(config.getMessageBrokerId());
            if (messageBroker == null) {
                logErrorMessage("Cannot mark " + channel + " as galactic.",
                        "Cannot find message broker with id: " + config.getMessageBrokerId());
                return false;
            }

            GalacticChannelData galacticChannel = new GalacticChannelData(config, messageBroker);
            this.galacticChannelsMap.put(channel, galacticChannel);

            // Register a request listener which will forward all requests
            // to the message broker. This will create a local {@link Channel} instance
            // which will act as a proxy to the message broker.
            galacticChannel.requestListener = this.listenRequestStream(channel, message -> {
                boolean result;
                String errorMsg = "";
                try {
                    result = messageBroker.sendMessage(config, message.getPayload());
                } catch (Exception ex) {
                    errorMsg = ex.getMessage();
                    result = false;
                }
                if (!result) {
                    logErrorMessage("Failed to send galactic message to channel '" + channel + "' ", errorMsg);
                    MonitorObject mo = new MonitorObject(
                            MonitorType.MonitorDropped, channel, getName(), message);
                    this.api.getMonitorStream().send(new MessageObject<>(MessageType.MessageTypeRequest, mo));
                }
            });

            this.api.getMonitorStream().send(new MessageObject<>(MessageType.MessageTypeRequest,
                    new MonitorObject(MonitorType.MonitorNewGalacticChannel, channel, getName())));
        }
        return true;
    }

    @Override
    public boolean markChannelAsLocal(String channel) {
        GalacticChannelData galacticChannel = this.galacticChannelsMap.remove(channel);
        if (galacticChannel != null) {
            // Close the message broker channel connections
            galacticChannel.close();
            // Remove the channel reference which was created by the markChannelAsGalactic() API
            closeChannel(channel, getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean isGalacticChannel(String channel) {
        return this.galacticChannelsMap.containsKey(channel);
    }

    private void init() {
        if (useJazz) {
            this.logBannerMessage("\uD83C\uDF08", "Starting Transport with id ["
                    + this.id.toString() + "]");
        } else {
            this.logBannerMessage(">>", "Starting Transport with id [" + this.id.toString() + "]");
        }
        Map<String, Object> peerBeans = context.getBeansWithAnnotation(TransportService.class);
        for (Map.Entry<String, Object> entry : peerBeans.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof TransportEnabled) {
                this.logDebugMessage("Initializing Transport Service: " + value.getClass().getSimpleName());
                ((TransportEnabled) value).initialize();
            }
        }
    }

    private MessageHandler createMessageHandler(
            MessageObjectHandlerConfig config, boolean requestStream) {

        if (!requestStream) {
            final String channelName = config.getReturnChannel();
            // Check if the response channel is a galactic channel.
            GalacticChannelData galacticChannel = galacticChannelsMap.get(channelName);
            if (galacticChannel != null) {
                return createMessageHandlerForGalacticResponseChannel(
                        config, channelName, galacticChannel);
            }
        }
        return new MessageHandlerImpl(requestStream, config, this);
    }

    private MessageHandler createMessageHandlerForGalacticResponseChannel(
            MessageObjectHandlerConfig config, String channelName, GalacticChannelData galacticChannel) {

        galacticChannel.addResponseListener(new GalacticMessageHandler() {
            @Override
            public void onMessage(Object message) {
                sendResponseMessage(channelName, message);
            }

            @Override
            public void onError(Object error) {
                sendErrorMessage(channelName, error);
            }
        });
        return new MessageHandlerImpl(false, config, this,
                aVoid -> galacticChannel.removeResponseListener());
    }

    private MessageResponder createMessageResponder(MessageObjectHandlerConfig config) {
        return new MessageResponderImpl(config, this);
    }

    private static class GalacticChannelData {

        final GalacticChannelConfig config;
        final MessageBrokerConnector messageBroker;

        BusTransaction requestListener;

        private MessageBrokerSubscription brokerSubscription;

        private int responseListeners = 0;

        GalacticChannelData(GalacticChannelConfig config, MessageBrokerConnector messageBroker) {
            this.config = config;
            this.messageBroker = messageBroker;
        }

        /**
         * Called when a new local listener subscribes to the remote galactic channel.
         * All local listeners share a single MessageBrokerSubscription.
         */
        synchronized void addResponseListener(GalacticMessageHandler handler) {
            if (brokerSubscription != null) {
                // We already have a valid subscription to the external MessageBroker
                // channel, just increase the responseListeners reference counter.
                responseListeners++;
            } else {
                // This is the first listener, subscribe to the external channel.
                brokerSubscription = messageBroker.subscribeToChannel(config, handler);
                responseListeners++;
            }
        }

        /**
         * Called when a local listeners is removed.
         */
        synchronized void removeResponseListener() {
            responseListeners--;
            // Remove response listener. If it was the last one,
            // unsubscribe from the MessageBroker.
            if (responseListeners == 0 && brokerSubscription != null) {
                messageBroker.unsubscribeFromChannel(brokerSubscription);
                brokerSubscription = null;
            }
        }

        /**
         * Unsubscribe from the local request stream and the remote response stream.
         */
        synchronized void close() {
            if (requestListener != null) {
                requestListener.unsubscribe();
            }
            if (brokerSubscription != null) {
                messageBroker.unsubscribeFromChannel(brokerSubscription);
                responseListeners = 0;
            }
        }
    }
}
