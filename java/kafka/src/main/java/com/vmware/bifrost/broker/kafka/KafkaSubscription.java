/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.kafka;

import com.vmware.bifrost.broker.MessageBrokerSubscription;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

/**
 * MessageBrokerSubscription implementation for Kafka message broker.
 */
public class KafkaSubscription extends MessageBrokerSubscription {

   @Getter(AccessLevel.PACKAGE)
   @Setter(AccessLevel.PACKAGE)
   KafkaMessageListenerContainer listenerContainer;
}
