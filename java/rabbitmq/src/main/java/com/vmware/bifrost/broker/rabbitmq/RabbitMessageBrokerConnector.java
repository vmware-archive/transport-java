/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.rabbitmq;

import com.vmware.bifrost.broker.GalacticMessageHandler;
import com.vmware.bifrost.broker.MessageBrokerConnector;
import com.vmware.bifrost.core.util.Loggable;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.AbstractConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.util.ErrorHandler;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MessageBrokerConnector that can connect to single RabbitMQ server.
 */
public class RabbitMessageBrokerConnector extends Loggable
      implements MessageBrokerConnector<RabbitChannelConfig, RabbitSubscription> {

   private final String brokerId;

   private final ConnectionFactory connectionFactory;

   protected RabbitTemplate rabbitTemplate;

   public RabbitMessageBrokerConnector(ConnectionFactory connectionFactory) {
      this("RabbitMqBroker-" + UUID.randomUUID().toString(), connectionFactory);
   }

   public RabbitMessageBrokerConnector(String brokerId, ConnectionFactory connectionFactory) {
      this(brokerId, connectionFactory, new RabbitTemplate(connectionFactory));
   }

   public RabbitMessageBrokerConnector(
         ConnectionFactory connectionFactory, MessageConverter messageConverter) {

      this("RabbitMqBroker-" + UUID.randomUUID().toString(),
            connectionFactory, new RabbitTemplate(connectionFactory));
      rabbitTemplate.setMessageConverter(messageConverter);
   }

   public RabbitMessageBrokerConnector(
         ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate) {

      this("RabbitMqBroker-" + UUID.randomUUID().toString(), connectionFactory, rabbitTemplate);
   }

   public RabbitMessageBrokerConnector(
         String brokerId, ConnectionFactory connectionFactory, RabbitTemplate rabbitTemplate) {

      this.brokerId = brokerId;
      this.connectionFactory = connectionFactory;
      this.rabbitTemplate = rabbitTemplate;
   }

   private Executor _taskExecutor;

   private synchronized Executor getExecutor() {
      if (_taskExecutor == null) {
         _taskExecutor = Executors.newCachedThreadPool(
               new NamedThreadPoolFactory(Executors.defaultThreadFactory(), getMessageBrokerId()));
      }
      return _taskExecutor;
   }

   @Override
   public String getMessageBrokerId() {
      return this.brokerId;
   }

   @Override
   public RabbitSubscription subscribeToChannel(
         RabbitChannelConfig channelConfig, GalacticMessageHandler messageHandler) {

      if (StringUtils.isEmpty(channelConfig.getInboundQueue())) {
         return null;
      }

      SimpleMessageListenerContainer simpleMessageListenerContainer =
            new SimpleMessageListenerContainer();
      simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
      simpleMessageListenerContainer.addQueueNames(channelConfig.getInboundQueue());
      simpleMessageListenerContainer.setTaskExecutor(getExecutor());
      simpleMessageListenerContainer.setMessageListener(new MessageListener() {
         @Override
         public void onMessage(Message message) {
            try {
               Object messagePayload;
               try {
                  messagePayload = rabbitTemplate.getMessageConverter().fromMessage(message);
               } catch (MessageConversionException convEx) {
                  String errMsg = convEx.getMessage();
                  if (convEx.getCause() != null) {
                     errMsg += "\n" + convEx.getCause().getMessage();
                  }
                  logErrorMessage(
                        String.format("Failed to deserialize incoming RabbitMQ message for queue '%s'",
                              channelConfig.getInboundQueue()),
                        errMsg);
                  messageHandler.onError(convEx);
                  return;
               }

               messageHandler.onMessage(messagePayload);
            } catch (Exception ex) {}
         }
      });
      simpleMessageListenerContainer.setErrorHandler(new ErrorHandler() {
         @Override
         public void handleError(Throwable t) {
            try {
               messageHandler.onError(t);
            } catch (Exception ex) {}
         }
      });

      simpleMessageListenerContainer.start();
      return new RabbitSubscription(simpleMessageListenerContainer);
   }

   @Override
   public boolean unsubscribeFromChannel(RabbitSubscription subscription) {
      if (subscription != null && subscription.simpleMessageListenerContainer != null &&
            subscription.simpleMessageListenerContainer.isRunning()) {
         subscription.simpleMessageListenerContainer.stop();
         return true;
      }
      return false;
   }

   @Override
   public boolean sendMessage(RabbitChannelConfig channelConfig, Object message) {
      if (channelConfig == null || StringUtils.isEmpty(channelConfig.getOutboundQueue())) {
         return false;
      }
      if (!StringUtils.isEmpty(channelConfig.getOutboundExchange())) {
         this.rabbitTemplate.convertAndSend(
               channelConfig.getOutboundExchange(),
               channelConfig.getOutboundQueue(),
               message);
      } else {
         this.rabbitTemplate.convertAndSend(channelConfig.getOutboundQueue(), message);
      }
      return true;
   }

   @Override
   public void connectMessageBroker() {
      // Do nothing.
   }

   @Override
   public void disconnectMessageBroker() {
      if (this.connectionFactory instanceof AbstractConnectionFactory) {
         ((AbstractConnectionFactory)this.connectionFactory).destroy();
      }
   }

   /**
    * Create a new RabbitMQ galactic channel config which can be used only
    * for receiving messages.
    *
    * @param inboundQueue, the name of the queue for incoming messages.
    */
   public RabbitChannelConfig newGalacticChannelConfig(String inboundQueue) {
      return newGalacticChannelConfig(inboundQueue, null);
   }

   /**
    * Create a new RabbitMQ galactic channel config which can be used
    * for receiving and sending messages.
    *
    * @param inboundQueue, the name of the queue for incoming messages.
    * @param outboundQueue, the name of the queue for sending messages.
    */
   public RabbitChannelConfig newGalacticChannelConfig(String inboundQueue, String outboundQueue) {
      return newGalacticChannelConfig(inboundQueue, outboundQueue, null);
   }

   /**
    * Create a new RabbitMQ galactic channel config which can be used
    * for receiving and sending messages.
    *
    * @param inboundQueue, the name of the queue for incoming messages.
    * @param outboundQueue, the name of the queue for sending messages.
    * @param outboundExchange, the name of the exchange which will be used for
    *                          sending messages to the RabbitMQ server.
    */
   public RabbitChannelConfig newGalacticChannelConfig(
         String inboundQueue, String outboundQueue, String outboundExchange) {
      RabbitChannelConfig config = new RabbitChannelConfig(
            this.getMessageBrokerId(),
            inboundQueue,
            outboundQueue,
            outboundExchange);
      return config;
   }

   private static class NamedThreadPoolFactory implements ThreadFactory {

      private final ThreadFactory threadFactory;
      private final String threadNamePrefix;
      private final AtomicInteger threadCounter = new AtomicInteger(0);

      NamedThreadPoolFactory(ThreadFactory threadFactory, String threadNamePrefix) {
         this.threadNamePrefix = threadNamePrefix;
         this.threadFactory = threadFactory;
      }

      @Override
      public Thread newThread(Runnable r) {
         Thread thread = threadFactory.newThread(r);
         thread.setName(threadNamePrefix + "-" + threadCounter.getAndIncrement());
         return thread;
      }
   }
}
