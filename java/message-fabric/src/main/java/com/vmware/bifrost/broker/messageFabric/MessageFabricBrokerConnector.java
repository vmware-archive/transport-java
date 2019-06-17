/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.messageFabric;

import com.vmware.atlas.message.client.listener.MessageListener;
import com.vmware.atlas.message.client.listener.impl.AbstractListener;
import com.vmware.atlas.message.client.model.enums.MessageType;
import com.vmware.atlas.message.client.service.MessageClient;
import com.vmware.bifrost.broker.GalacticMessageHandler;
import com.vmware.bifrost.broker.MessageBrokerConnector;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MessageBrokerConnector that can connect to MessageFabric broker.
 */
public class MessageFabricBrokerConnector implements
      MessageBrokerConnector<MessageFabricChannelConfig, MessageFabricSubscription> {

   private final String brokerId;
   private final MessageClient messageClient;

   private final Map<String, AtomicInteger> topicSubscriptions = new HashMap<>();

   public MessageFabricBrokerConnector(MessageClient messageClient) {
      this("MessageFabricBroker-" + UUID.randomUUID().toString(), messageClient);
   }

   public MessageFabricBrokerConnector(String brokerId, MessageClient messageClient) {
      this.brokerId = brokerId;
      this.messageClient = messageClient;
   }

   @Override
   public String getMessageBrokerId() {
      return this.brokerId;
   }

   @Override
   @SuppressWarnings("unchecked")
   public MessageFabricSubscription subscribeToChannel(
         MessageFabricChannelConfig channelConfig, GalacticMessageHandler messageHandler) {

      if (channelConfig == null || messageHandler == null ||
            StringUtils.isEmpty(channelConfig.getDestinationTopicOrServiceName())) {
         return null;
      }
      String destination = channelConfig.getDestinationTopicOrServiceName();
      MessageListener messageListener;
      if (!channelConfig.isDirect()) {
         synchronized (topicSubscriptions) {
            if (!topicSubscriptions.containsKey(destination)) {
               topicSubscriptions.put(destination, new AtomicInteger(0));
            }
            if (topicSubscriptions.get(destination).incrementAndGet() == 1) {
               try {
                  messageClient.subscribe(destination);
               } catch (RuntimeException ex) {
                  topicSubscriptions.get(destination).decrementAndGet();
                  return null;
               }
            }
         }

         messageListener = new BifrostMessageListener(messageHandler,
               channelConfig.getDestinationTopicOrServiceName(),
               channelConfig.getPayloadType(), channelConfig.getMessageType());
      } else {
         messageListener = new BifrostMessageListener(messageHandler,
               channelConfig.getPayloadType(), channelConfig.getMessageType());
      }

      messageClient.addListeners(messageListener);
      return new MessageFabricSubscription(channelConfig, messageListener);
   }

   @Override
   public boolean unsubscribeFromChannel(MessageFabricSubscription subscription) {
      synchronized (this.topicSubscriptions) {
         if (subscription != null && subscription.listener != null) {
            messageClient.removeListener(subscription.listener);
            subscription.listener = null;

            if (!subscription.channelConfig.isDirect()) {
               String topic = subscription.channelConfig.getDestinationTopicOrServiceName();

               if (this.topicSubscriptions.get(topic).decrementAndGet() == 0) {
                  messageClient.unsubscribe(topic);
               }

            }
            return true;
         }
      }
      return false;
   }

   @Override
   public boolean sendMessage(MessageFabricChannelConfig channelConfig, Object message) {
      if (message != null && channelConfig != null && channelConfig.getPayloadType() != null &&
            !StringUtils.isEmpty(channelConfig.getDestinationTopicOrServiceName()) &&
            channelConfig.getPayloadType().isInstance(message)) {

         if (channelConfig.isDirect()) {
            messageClient.sendDirectAsync(channelConfig.getMessageType(),
                  channelConfig.getDestinationTopicOrServiceName(),
                  (Serializable) message);
         } else {
            messageClient.sendAsync(channelConfig.getMessageType(),
                  channelConfig.getDestinationTopicOrServiceName(),
                  (Serializable) message);
         }
         return true;
      }
      return false;
   }

   @Override
   public void connectMessageBroker() {
      messageClient.connect();
   }

   @Override
   public void disconnectMessageBroker() {
      messageClient.disconnect();
   }

   public MessageFabricChannelConfig newDirectMessageFabricChannelConfig(
         MessageType messageType, String serviceName, Class<? extends Serializable> payloadType) {
      return new MessageFabricChannelConfig(
            this.getMessageBrokerId(), payloadType, messageType, serviceName, true);
   }

   public MessageFabricChannelConfig newTopicMessageFabricChannelConfig(
         MessageType messageType, String topic, Class<? extends Serializable> payloadType) {
      return new MessageFabricChannelConfig(
            this.getMessageBrokerId(), payloadType, messageType, topic, false);
   }

   private static class BifrostMessageListener<T> extends AbstractListener<T> {

      private final GalacticMessageHandler messageHandler;

      BifrostMessageListener(
            GalacticMessageHandler messageHandler,
            String topic, Class<T> payloadType, MessageType messageType) {
         super(topic, payloadType, messageType);

         this.messageHandler = messageHandler;
      }

      BifrostMessageListener(
            GalacticMessageHandler messageHandler,
            Class<T> payloadType, MessageType messageType) {
         super(payloadType, messageType);

         this.messageHandler = messageHandler;
      }

      @Override
      public void onMessage(T payload) {
         messageHandler.onMessage(payload);
      }
   }
}
