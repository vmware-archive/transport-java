/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.kafka;

import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

/**
 * Contains utility methods for the KafkaMessageBrokerConnector.
 */
public class KafkaUtil {

   private KafkaUtil() {}

   /**
    * Wraps a message in a KafkaMessageWrapper and allows to associate key with it.
    * This allows sending messages with keys to the Kafka broker.
    */
   public static KafkaMessageWrapper wrapMessage(Object key, Object message) {
      return new KafkaMessageWrapper(key, message);
   }

   /**
    * Creates a KafkaTemplate using a DefaultKafkaProducerFactory.
    *
    * @param kafkaProducerConfig, a map holding the configuration of a Kafka Producer.
    *   see {@link org.apache.kafka.clients.producer.ProducerConfig} for the supported
    *   configuration properties.
    */
   public static KafkaTemplate createDefaultKafkaTemplate(Map<String, Object> kafkaProducerConfig) {
      ProducerFactory<Object, Object> pf =
            new DefaultKafkaProducerFactory<>(kafkaProducerConfig);
      KafkaTemplate<Object, Object> template = new KafkaTemplate<>(pf);
      return template;
   }
}
