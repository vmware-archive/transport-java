/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.messageFabric;

import com.vmware.atlas.message.client.listener.MessageListener;
import com.vmware.bifrost.broker.MessageBrokerSubscription;

/**
 * MessageBrokerSubscription implementation for MessageFabric client.
 */
public class MessageFabricSubscription extends MessageBrokerSubscription {

   MessageListener listener;

   final MessageFabricChannelConfig channelConfig;

   MessageFabricSubscription(MessageFabricChannelConfig channelConfig, MessageListener listener) {
      this.listener = listener;
      this.channelConfig = channelConfig;
   }
}
