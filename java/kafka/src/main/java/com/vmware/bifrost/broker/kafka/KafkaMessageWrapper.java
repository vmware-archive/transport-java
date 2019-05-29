/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.kafka;

import lombok.Getter;

/**
 * Wrapper class allowing to send/receive Kafka message with keys.
 */
public class KafkaMessageWrapper {

   @Getter
   private final Object key;

   @Getter
   private final Object message;

   public KafkaMessageWrapper(Object key, Object message) {
      this.key = key;
      this.message = message;
   }
}
