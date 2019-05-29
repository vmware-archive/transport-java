/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.kafka;

import com.vmware.bifrost.broker.GalacticMessageHandler;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.GenericErrorHandler;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;

public class KafkaMessageBrokerConnectorTest {

   private Object galacticMessage;
   private int counter = 0;
   private int errorCounter = 0;

   @Before
   public void before() {
      this.galacticMessage = null;
      this.counter = 0;
      this.errorCounter = 0;
   }

   @Test
   public void testCreate() {

      KafkaMessageBrokerConnector kafkaMb = new KafkaMessageBrokerConnector();
      Assert.assertNotNull(kafkaMb.getMessageBrokerId());

      kafkaMb = new KafkaMessageBrokerConnector("broker-id");
      Assert.assertEquals("broker-id", kafkaMb.getMessageBrokerId());

      kafkaMb.connectMessageBroker();
      kafkaMb.disconnectMessageBroker();
   }

   @Test
   public void testSendMessage() {

      KafkaMessageBrokerConnector kafkaMb = new KafkaMessageBrokerConnector();
      Assert.assertFalse(kafkaMb.sendMessage(null, "test"));

      KafkaChannelConfig conf = kafkaMb.newKafkaChannelConfig("test-topic", (KafkaTemplate) null);
      Assert.assertFalse(kafkaMb.sendMessage(conf, "test"));

      KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
      Mockito.when(template.send("test-topic", "test")).thenReturn(null);
      conf = kafkaMb.newKafkaChannelConfig("test-topic", template);
      Assert.assertTrue(kafkaMb.sendMessage(conf, "test"));
      Mockito.verify(template).send("test-topic", "test");

      KafkaMessageWrapper wrapper = new KafkaMessageWrapper("message-key", "test-message");
      Mockito.when(template.send("test-topic", "message-key", "test-message")).thenReturn(null);
      Assert.assertTrue(kafkaMb.sendMessage(conf, wrapper));
      Mockito.verify(template).send("test-topic", "message-key", "test-message");
   }

   @Test
   public void testSubscribeToChannel() {
      KafkaMessageBrokerConnector kafkaMb = new KafkaMessageBrokerConnector();

      GalacticMessageHandler handler = Mockito.spy(new GalacticMessageHandler() {
         @Override
         public void onMessage(Object message) {
            galacticMessage = message;
            counter++;
         }

         @Override
         public void onError(Object error) {
            errorCounter++;
         }
      });

      Assert.assertNull(kafkaMb.subscribeToChannel(null, null));
      Assert.assertNull(kafkaMb.subscribeToChannel(
            kafkaMb.newKafkaChannelConfig("test-topic", (ConsumerFactory) null), handler));

      ConsumerFactory consumerFactory = Mockito.mock(ConsumerFactory.class);
      Mockito.when(consumerFactory.createConsumer(Mockito.any(), Mockito.any()))
            .thenReturn(Mockito.mock(Consumer.class));

      KafkaChannelConfig conf = kafkaMb.newKafkaChannelConfig("test-topic", consumerFactory);
      Assert.assertNull(kafkaMb.subscribeToChannel(conf, null));

      KafkaSubscription subscription = kafkaMb.subscribeToChannel(conf, handler);
      Assert.assertNotNull(subscription);
      Assert.assertNotNull(subscription.getListenerContainer());
      ContainerProperties containerProperties = subscription.getListenerContainer().getContainerProperties();

      Assert.assertEquals(1, containerProperties.getTopics().length);
      Assert.assertEquals("test-topic", containerProperties.getTopics()[0]);

      ConsumerRecord cr = new ConsumerRecord<>("test-topic", 0, 0, "test-key", "test-value");

      ((MessageListener)containerProperties.getMessageListener()).onMessage(cr);

      Assert.assertEquals(galacticMessage, "test-value");
      Assert.assertEquals(1, counter);

      ((GenericErrorHandler<ConsumerRecord>) containerProperties.getGenericErrorHandler())
            .handle(new Exception("test-ex"), cr);
      Assert.assertEquals(1, errorCounter);

      Assert.assertTrue(kafkaMb.unsubscribeFromChannel(subscription));
      Assert.assertFalse(subscription.listenerContainer.isRunning());

      // Throw second exception and verify that it's ignored
      ((GenericErrorHandler<ConsumerRecord>) containerProperties.getGenericErrorHandler())
            .handle(new Exception("test-ex"), cr);
      Assert.assertEquals(1, errorCounter);

      // Try to unsubscribe for a second time.
      Assert.assertFalse(kafkaMb.unsubscribeFromChannel(subscription));

   }

   @Test
   public void testSubscribeToChannelWrapIncomingMessages() {
      KafkaMessageBrokerConnector kafkaMb = new KafkaMessageBrokerConnector();

      GalacticMessageHandler handler = Mockito.spy(new GalacticMessageHandler() {
         @Override
         public void onMessage(Object message) {
            galacticMessage = message;
            counter++;
         }

         @Override
         public void onError(Object error) {
            errorCounter++;
         }
      });

      ConsumerFactory consumerFactory = Mockito.mock(ConsumerFactory.class);
      Mockito.when(consumerFactory.createConsumer(Mockito.any(), Mockito.any()))
            .thenReturn(Mockito.mock(Consumer.class));

      KafkaChannelConfig conf = kafkaMb.newKafkaChannelConfig("test-topic", consumerFactory, true,
            AbstractMessageListenerContainer.AckMode.RECORD, null);

      KafkaSubscription subscription = kafkaMb.subscribeToChannel(conf, handler);
      Assert.assertNotNull(subscription);
      Assert.assertNotNull(subscription.getListenerContainer());
      ContainerProperties containerProperties = subscription.getListenerContainer().getContainerProperties();

      Assert.assertEquals(containerProperties.getAckMode(), AbstractMessageListenerContainer.AckMode.RECORD);

      ConsumerRecord cr = new ConsumerRecord<>("test-topic", 0, 0, "test-key", "test-value");

      ((MessageListener)containerProperties.getMessageListener()).onMessage(cr);

      Assert.assertTrue(galacticMessage instanceof KafkaMessageWrapper);
      Assert.assertEquals( ((KafkaMessageWrapper)galacticMessage).getKey(), "test-key");
      Assert.assertEquals( ((KafkaMessageWrapper)galacticMessage).getMessage(), "test-value");
      Assert.assertEquals(1, counter);
   }

   @Test
   public void testNewKafkaChannelConfig() {
      KafkaMessageBrokerConnector kafkaMb = new KafkaMessageBrokerConnector();

      KafkaTemplate template = Mockito.mock(KafkaTemplate.class);
      ConsumerFactory consumerFactory = Mockito.mock(ConsumerFactory.class);

      Assert.assertNull(kafkaMb.newKafkaChannelConfig(null, template));

      KafkaChannelConfig conf = kafkaMb.newKafkaChannelConfig(
            "topic", consumerFactory, true, template);

      Assert.assertNotNull(conf);
      Assert.assertEquals(conf.getTopic(), "topic");
      Assert.assertEquals(conf.isWrapIncomingMessages(), true);
      Assert.assertEquals(conf.getMessageBrokerId(), kafkaMb.getMessageBrokerId());
   }
}
