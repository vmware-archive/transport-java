/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.kafka;

import com.vmware.bifrost.broker.GalacticChannelConfig;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

/**
 * GalacticChannelConfig implementation for Kafka message broker.
 */
public class KafkaChannelConfig extends GalacticChannelConfig {

   @Getter(AccessLevel.PACKAGE)
   private final String topic;

   @Getter(AccessLevel.PACKAGE)
   private final ConsumerFactory consumerFactory;

   @Getter(AccessLevel.PACKAGE)
   private final boolean wrapIncomingMessages;

   @Getter(AccessLevel.PACKAGE)
   private final AbstractMessageListenerContainer.AckMode consumerListenerAckMode;

   @Getter(AccessLevel.PACKAGE)
   private final KafkaTemplate kafkaTemplate;

   KafkaChannelConfig(String messageBrokerId, String topic, ConsumerFactory consumerFactory,
         boolean wrapIncomingMessages, KafkaTemplate kafkaTemplate,
         AbstractMessageListenerContainer.AckMode consumerListenerAckMode) {

      super(messageBrokerId);
      this.topic = topic;
      this.consumerFactory = consumerFactory;
      this.kafkaTemplate = kafkaTemplate;
      this.wrapIncomingMessages = wrapIncomingMessages;
      this.consumerListenerAckMode = consumerListenerAckMode;
   }
}
