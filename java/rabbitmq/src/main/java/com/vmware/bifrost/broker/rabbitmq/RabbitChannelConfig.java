/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.rabbitmq;

import com.vmware.bifrost.broker.GalacticChannelConfig;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * GalacticChannelConfig implementation for RabbitMQ message broker.
 */
public class RabbitChannelConfig extends GalacticChannelConfig {

   @Getter(AccessLevel.PACKAGE)
   private final String inboundQueue;

   @Getter(AccessLevel.PACKAGE)
   private final String outboundQueue;

   @Getter(AccessLevel.PACKAGE)
   private final String outboundExchange;

   RabbitChannelConfig(String messageBrokerId,
         String inboundQueue, String outboundQueue, String outboundExchange) {
      super(messageBrokerId);

      this.inboundQueue = inboundQueue;
      this.outboundExchange = outboundExchange;
      this.outboundQueue = outboundQueue;
   }
}
