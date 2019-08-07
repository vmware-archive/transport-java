/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.rabbitmq;

import com.vmware.bifrost.broker.GalacticMessageHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

public class RabbitMessageBrokerConnectorTest {

   private CachingConnectionFactory connectionFactory;
   private Object galacticMessage;
   private int counter = 0;
   private int errorCounter = 0;

   @Before
   public void before() {
      this.connectionFactory = Mockito.spy(CachingConnectionFactory.class);
      this.galacticMessage = null;
      this.counter = 0;
   }

   @Test
   public void testCreate() {
      RabbitMessageBrokerConnector rmq = new RabbitMessageBrokerConnector(connectionFactory);
      Assert.assertNotNull(rmq.getMessageBrokerId());

      rmq = new RabbitMessageBrokerConnector("rmq-id", connectionFactory);
      Assert.assertEquals(rmq.getMessageBrokerId(), "rmq-id");

      MessageConverter messageConverter = new Jackson2JsonMessageConverter();

      rmq = new RabbitMessageBrokerConnector(connectionFactory, messageConverter);
      Assert.assertNotNull(rmq.getMessageBrokerId());
      Assert.assertEquals(rmq.rabbitTemplate.getMessageConverter(), messageConverter);

      RabbitTemplate rabbitTemplate = new RabbitTemplate(this.connectionFactory);
      rmq = new RabbitMessageBrokerConnector(connectionFactory, rabbitTemplate);
      Assert.assertEquals(rmq.rabbitTemplate, rabbitTemplate);
   }

   @Test
   public void testDestroy() {
      RabbitMessageBrokerConnector rmq = new RabbitMessageBrokerConnector(connectionFactory);
      rmq.connectMessageBroker();
      rmq.disconnectMessageBroker();
      Mockito.verify(this.connectionFactory).destroy();
      // Verify that no error will be thrown for non-AbstractConnectionFactory connection factories.
      rmq = new RabbitMessageBrokerConnector(Mockito.spy(ConnectionFactory.class));
      rmq.disconnectMessageBroker();
   }

   @Test
   public void testSendMessage() {
      RabbitTemplate template = Mockito.spy(RabbitTemplate.class);

      RabbitMessageBrokerConnector rmq = new RabbitMessageBrokerConnector(connectionFactory, template);

      Assert.assertFalse(rmq.sendMessage(null, "test"));

      RabbitChannelConfig conf1 = rmq.newGalacticChannelConfig("inbound-q");
      Assert.assertFalse(rmq.sendMessage(conf1, "test"));

      RabbitChannelConfig conf2 = rmq.newGalacticChannelConfig("inbound-q", "outbound-q");
      Mockito.doNothing().when(template).convertAndSend("outbound-q", "test");
      Assert.assertTrue(rmq.sendMessage(conf2, "test"));
      Mockito.verify(template).convertAndSend("outbound-q", "test");

      RabbitChannelConfig conf3 = rmq.newGalacticChannelConfig("", "outbound-q2", "outbound-exchange");
      Mockito.doNothing().when(template).convertAndSend("outbound-exchange", "outbound-q2", "test-message2");
      Assert.assertTrue(rmq.sendMessage(conf3, "test-message2"));
      Mockito.verify(template).convertAndSend("outbound-exchange", "outbound-q2", "test-message2");
   }

   @Test
   public void testSubscribeToChannel() {
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


      ConnectionFactory connectionFactory = Mockito.spy(ConnectionFactory.class);
      Connection connection = Mockito.spy(Connection.class);
      Mockito.when(connectionFactory.createConnection()).thenReturn(connection);

      MessageConverter messageConverter = new MessageConverter() {
         @Override
         public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
            return null;
         }

         @Override
         public Object fromMessage(Message message) throws MessageConversionException {
            return "converted-message";
         }
      };
      RabbitMessageBrokerConnector rmq = new RabbitMessageBrokerConnector(connectionFactory, messageConverter);

      Assert.assertNull(rmq.subscribeToChannel(rmq.newGalacticChannelConfig(null), null));

      RabbitChannelConfig conf = rmq.newGalacticChannelConfig("inbound-q");
      RabbitSubscription sub = rmq.subscribeToChannel(conf, handler);
      Assert.assertNotNull(sub);
      Assert.assertNotNull(sub.simpleMessageListenerContainer);

      RabbitSubscription sub2 = rmq.subscribeToChannel(conf, handler);

      Assert.assertNotEquals(sub2.simpleMessageListenerContainer, sub.simpleMessageListenerContainer);

      ((MessageListener) sub.simpleMessageListenerContainer.getMessageListener()).onMessage(
            new Message(new byte[0], new MessageProperties()));
      Assert.assertEquals(galacticMessage, "converted-message");
      Assert.assertEquals(1, counter);

      Assert.assertTrue(sub.simpleMessageListenerContainer.isRunning());

      Assert.assertTrue(rmq.unsubscribeFromChannel(sub));
      Assert.assertFalse(sub.simpleMessageListenerContainer.isRunning());

      Assert.assertFalse(rmq.unsubscribeFromChannel(sub));

      Assert.assertFalse(rmq.unsubscribeFromChannel(null));
   }

   @Test
   public void testSubscribeToChannelSerializationError() {
      GalacticMessageHandler handler = Mockito.spy(new GalacticMessageHandler() {
         @Override
         public void onMessage(Object message) {
            galacticMessage = message;
            counter++;
         }

         @Override
         public void onError(Object error) {
            errorCounter++;
            galacticMessage = error;
         }
      });


      ConnectionFactory connectionFactory = Mockito.spy(ConnectionFactory.class);
      Connection connection = Mockito.spy(Connection.class);
      Mockito.when(connectionFactory.createConnection()).thenReturn(connection);

      MessageConverter messageConverter = new MessageConverter() {
         @Override
         public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
            return null;
         }

         @Override
         public Object fromMessage(Message message) throws MessageConversionException {
            throw new MessageConversionException("failed-to-convert-error", new Exception("cause-error"));
         }
      };
      RabbitMessageBrokerConnector rmq = new RabbitMessageBrokerConnector(connectionFactory, messageConverter);

      RabbitChannelConfig conf = rmq.newGalacticChannelConfig("inbound-q");
      RabbitSubscription sub = rmq.subscribeToChannel(conf, handler);

      ((MessageListener) sub.simpleMessageListenerContainer.getMessageListener()).onMessage(
            new Message(new byte[0], new MessageProperties()));

      Assert.assertEquals(0, counter);
      Assert.assertEquals(1, errorCounter);
      Assert.assertEquals("failed-to-convert-error", ((Throwable) galacticMessage).getMessage());

      Assert.assertTrue(sub.simpleMessageListenerContainer.isRunning());
   }
}
