/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.kafka;

import com.vmware.bifrost.broker.GalacticMessageHandler;
import com.vmware.bifrost.broker.MessageBrokerConnector;
import com.vmware.bifrost.core.util.Loggable;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * MessageBrokerConnector that can connect to single Kafka server.
 */
public class KafkaMessageBrokerConnector extends Loggable
      implements MessageBrokerConnector<KafkaChannelConfig, KafkaSubscription> {

   private final String brokerId;

   public KafkaMessageBrokerConnector() {
      this("KafkaBroker-" + UUID.randomUUID().toString());
   }

   public KafkaMessageBrokerConnector(String brokerId) {
      this.brokerId = brokerId;
   }

   @Override
   public String getMessageBrokerId() {
      return brokerId;
   }

   @Override
   public KafkaSubscription subscribeToChannel(
         final KafkaChannelConfig channelConfig, GalacticMessageHandler handler) {

      if (channelConfig == null || channelConfig.getConsumerFactory() == null || handler == null) {
         return null;
      }

      final KafkaSubscription kafkaSubscription = new KafkaSubscription();

      ContainerProperties containerProperties = new ContainerProperties(channelConfig.getTopic());
      if (channelConfig.getConsumerListenerAckMode() != null) {
         containerProperties.setAckMode(channelConfig.getConsumerListenerAckMode());
      }

      containerProperties.setGenericErrorHandler(new ErrorHandler() {
         @Override
         public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {
            // Ignore errors for stopped containers
            if (kafkaSubscription.listenerContainer != null &&
                  kafkaSubscription.listenerContainer.isRunning()) {
               logErrorMessage("Error while processing ConsumerRecord for topic: " + data.topic(),
                     thrownException.getMessage());
               handler.onError(thrownException);
            }
         }
      });
      containerProperties.setMessageListener(new MessageListener<Object, Object>() {
         @Override
         public void onMessage(ConsumerRecord<Object, Object> message) {
            if (channelConfig.isWrapIncomingMessages()) {
               handler.onMessage(KafkaUtil.wrapMessage(message.key(), message.value()));
            } else {
               handler.onMessage(message.value());
            }
         }
      });

      KafkaMessageListenerContainer<Object, Object> container =
            new KafkaMessageListenerContainer<>(
                  channelConfig.getConsumerFactory(), containerProperties);
      container.start();
      kafkaSubscription.setListenerContainer(container);
      return kafkaSubscription;
   }

   @Override
   public boolean unsubscribeFromChannel(KafkaSubscription subscription) {
      if (subscription.listenerContainer != null && subscription.listenerContainer.isRunning()) {
         subscription.listenerContainer.stop();
         return true;
      }
      return false;
   }

   @Override
   public boolean sendMessage(KafkaChannelConfig channelConfig, Object payload) {
      if (channelConfig != null && channelConfig.getKafkaTemplate() != null) {
         if (payload instanceof KafkaMessageWrapper) {
            KafkaMessageWrapper wrapper = (KafkaMessageWrapper) payload;
            channelConfig.getKafkaTemplate().send(
                  channelConfig.getTopic(), wrapper.getKey(), wrapper.getMessage());
         } else {
            channelConfig.getKafkaTemplate().send(channelConfig.getTopic(), payload);
         }
         return true;
      }
      return false;
   }

   @Override
   public void connectMessageBroker() {
      // do nothing
   }

   @Override
   public void disconnectMessageBroker() {
      // do nothing
   }

   /**
    * Create a new Kafka galactic channel config which can be used
    * only for receiving messages from a given topic.
    *
    * @param topic, the name of the Kafka topic
    * @param consumerFactory, {@link ConsumerFactory} used to create consumer listeners.
    */
   public KafkaChannelConfig newKafkaChannelConfig(
         String topic,
         ConsumerFactory consumerFactory) {

      return this.newKafkaChannelConfig(
            topic,
            consumerFactory,
            false,
            null,
            null);
   }

   /**
    * Create a new Kafka galactic channel config which can be used
    * only for sending messages to a given topic.
    *
    * @param topic, the name of the Kafka topic
    * @param kafkaTemplate, {@link KafkaTemplate} instance which will be used to send messages.
    */
   public KafkaChannelConfig newKafkaChannelConfig(
         String topic,
         KafkaTemplate kafkaTemplate) {

      return this.newKafkaChannelConfig(
            topic,
            null,
            false,
            null,
            kafkaTemplate);
   }

   /**
    * Create a new Kafka galactic channel config which can be used
    * for receiving and sending messages.
    *
    * @param topic, the name of the Kafka topic
    * @param consumerFactory, {@link ConsumerFactory} used to create consumer listeners.
    * @param wrapIncomingMessages, boolean flag controlling whether incoming messages should be wrapped
    *     in a {@link KafkaMessageWrapper}. If not set, the GalacticMessageHandler will be called
    *     only with the Kafka message's value. If set to true, the GalacticMessageHandler will be called
    *     with KafkaMessageWrapper instance containing both the value and key of the message.
    * @param kafkaTemplate, {@link KafkaTemplate} instance which will be used to send messages.
    */
   public KafkaChannelConfig newKafkaChannelConfig(
         String topic,
         ConsumerFactory consumerFactory,
         boolean wrapIncomingMessages,
         KafkaTemplate kafkaTemplate) {

      return this.newKafkaChannelConfig(
            topic,
            consumerFactory,
            wrapIncomingMessages,
            null,
            kafkaTemplate);
   }

   /**
    * Create a new Kafka galactic channel config which can be used
    * for receiving and sending messages.
    *
    * @param topic, the name of the Kafka topic
    * @param consumerFactory, {@link ConsumerFactory} used to create consumer listeners.
    * @param wrapIncomingMessages, boolean flag conrolling whether incoming messages should be wrapped
    *     in a {@link KafkaMessageWrapper}. If not set, the GalacticMessageHandler will be called
    *     only the Kafka message's value. If set to true, the GalacticMessageHandler will be called
    *     with KafkaMessageWrapper instance containing both the value and key of the message.
    * @param consumerListenerAckMode, optional acknowledge mode that will be used by consumer listeners.
    * @param kafkaTemplate, {@link KafkaTemplate} instance which will be used to send messages.
    */
   public KafkaChannelConfig newKafkaChannelConfig(
         String topic,
         ConsumerFactory consumerFactory,
         boolean wrapIncomingMessages,
         AbstractMessageListenerContainer.AckMode consumerListenerAckMode,
         KafkaTemplate kafkaTemplate) {

      if (StringUtils.isEmpty(topic)) {
         return null;
      }
      return new KafkaChannelConfig(
            this.getMessageBrokerId(), topic, consumerFactory,
            wrapIncomingMessages, kafkaTemplate, consumerListenerAckMode);
   }
}
