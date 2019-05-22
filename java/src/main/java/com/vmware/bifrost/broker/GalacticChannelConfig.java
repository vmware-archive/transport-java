/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker;

import lombok.Getter;

/**
 * Base class for galactic channel configuration.
 *
 * Each concrete MessageBrokerConnector implementation should define its own
 * GalacticChannelConfig class containing channel information specific to the
 * MessageBroker, i.e. a RabbitMQ channel config will store different data
 * than a Kafka channel config.
 */
public abstract class GalacticChannelConfig {

   /**
    * The id of the MessageBrokerConnector instance associated
    * with this galactic channel.
    */
   @Getter
   private final String messageBrokerId;

   public GalacticChannelConfig(String messageBrokerId) {
      this.messageBrokerId = messageBrokerId;
   }
}
