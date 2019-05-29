/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package kafkaSamples.config;

import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfigurer;
import com.vmware.bifrost.broker.GalacticChannelConfig;
import com.vmware.bifrost.broker.kafka.KafkaMessageBrokerConnector;
import com.vmware.bifrost.bus.EventBus;
import kafkaSamples.RequestLogEntry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BifrostConfig implements BifrostBridgeConfigurer {

   private final String KAFKA_SERVER = "localhost:9092";

   @Autowired
   EventBus eventBus;

   @Override
   public void configureGalacticChannels() {

      KafkaMessageBrokerConnector kafkaMbc = new KafkaMessageBrokerConnector();
      eventBus.registerMessageBroker(kafkaMbc);

      DefaultKafkaConsumerFactory<String, String> cf =
            new DefaultKafkaConsumerFactory<>(consumerProps(),
                  new StringDeserializer(), new StringDeserializer());
      GalacticChannelConfig requestChannelConf = kafkaMbc.newKafkaChannelConfig(
            "request-q", cf, true, createTemplate(StringSerializer.class));
      eventBus.markChannelAsGalactic(GalacticChannels.REQUEST_CHANNEL, requestChannelConf);

      GalacticChannelConfig responseChannelConf = kafkaMbc.newKafkaChannelConfig(
            "response-q", cf, false,
            AbstractMessageListenerContainer.AckMode.RECORD,
            createTemplate(StringSerializer.class));
      eventBus.markChannelAsGalactic(GalacticChannels.RESPONSE_CHANNEL, responseChannelConf);

      DefaultKafkaConsumerFactory<String, RequestLogEntry> jsonCf =
            new DefaultKafkaConsumerFactory<>(
                  consumerProps(), new StringDeserializer(),
                  new JsonDeserializer<>(RequestLogEntry.class));
      GalacticChannelConfig logChannelConf = kafkaMbc.newKafkaChannelConfig(
            "request-log-q", jsonCf, false,
            createTemplate(JsonSerializer.class));
      eventBus.markChannelAsGalactic(GalacticChannels.REQUEST_LOG_CHANNEL, logChannelConf);
   }

   private KafkaTemplate createTemplate(Class valueSerializer) {
      Map<String, Object> senderProps = senderProps(valueSerializer);
      ProducerFactory<Object, Object> pf =
            new DefaultKafkaProducerFactory<>(senderProps);
      KafkaTemplate<Object, Object> template = new KafkaTemplate<>(pf);
      return template;
   }

   private Map<String, Object> senderProps(Class valueSerializer) {
      Map<String, Object> props = new HashMap<>();
      props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
      props.put(ProducerConfig.RETRIES_CONFIG, 0);
      props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
      props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
      props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
      props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
      props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
      return props;
   }

   private Map<String, Object> consumerProps() {
      Map<String, Object> props = new HashMap<>();
      props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
      props.put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-sample-app");
      props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
      props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
      props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
      return props;
   }
}
