/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.rabbitmq;

import com.vmware.bifrost.broker.MessageBrokerSubscription;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;

/**
 * MessageBrokerSubscription implementation for RabbitMQ message broker.
 */
public class RabbitSubscription extends MessageBrokerSubscription {

   /**
    * A listener container associated with a single RabbitMQ queue.
    */
   final SimpleMessageListenerContainer simpleMessageListenerContainer;

   RabbitSubscription(SimpleMessageListenerContainer simpleMessageListenerContainer) {
      this.simpleMessageListenerContainer = simpleMessageListenerContainer;
   }
}
