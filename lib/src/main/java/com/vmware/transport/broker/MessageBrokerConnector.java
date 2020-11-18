/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.broker;

/**
 * MessageBrokerConnector interface allows extending the Transport EventBus by
 * connecting to external MessageBrokers like RabbitMQ.
 */
public interface MessageBrokerConnector<T extends GalacticChannelConfig,
      S extends MessageBrokerSubscription> {

   /**
    * Returns the unique id of the MessageBrokerConnector instance.
    */
   String getMessageBrokerId();

   /**
    * Start listening for incoming messages on external channel.
    *
    * @param channelConfig, the galactic channel configuration.
    * @param handler, callback interface that handles incoming messages and errors.
    * @return MessageBrokerSubscription which can be used to unsubscribe from the channel.
    */
   S subscribeToChannel(T channelConfig, GalacticMessageHandler handler);

   /**
    * Unsubscribe from external channel.
    * @param subscription, MessageBrokerSubscription returned from the subscribeToChannel() API.
    * @return true if the unsubscribe operation was successful.
    */
   boolean unsubscribeFromChannel(S subscription);

   /**
    * Send message to external channel.
    * @param channelConfig, the galactic channel configuration.
    * @param payload, the message to be send.
    * @return true if the message was send successfully.
    */
   boolean sendMessage(T channelConfig, Object payload);

   /**
    * Connects to the external message broker.
    */
   void connectMessageBroker();

   /**
    * Disconnects from the external message broker.
    */
   void disconnectMessageBroker();
}
