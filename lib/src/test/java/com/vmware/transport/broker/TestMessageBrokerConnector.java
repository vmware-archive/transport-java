/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.broker;

import java.util.ArrayList;
import java.util.List;

public class TestMessageBrokerConnector implements
      MessageBrokerConnector<TestGalacticChannelConfig, TestGalacticSubscription> {

   private String brokerId;

   public boolean connected = false;

   public Object lastSentMessage;
   public TestGalacticChannelConfig lastSentMessageChannel;
   public int messagesSent = 0;

   public List<TestGalacticSubscription> subscriptions = new ArrayList<>();

   public TestMessageBrokerConnector(String id) {
      this.brokerId = id;
   }

   @Override
   public String getMessageBrokerId() {
      return this.brokerId;
   }

   @Override
   public TestGalacticSubscription subscribeToChannel(
         TestGalacticChannelConfig channelConfig, GalacticMessageHandler callback) {

      TestGalacticSubscription sub = new TestGalacticSubscription(channelConfig.remoteChannel, callback);
      subscriptions.add(sub);
      return sub;
   }

   @Override
   public boolean unsubscribeFromChannel(TestGalacticSubscription subscription) {
      subscriptions.remove(subscription);
      return true;
   }

   @Override
   public boolean sendMessage(TestGalacticChannelConfig channelConfig, Object payload) {
      this.lastSentMessage = payload;
      this.lastSentMessageChannel = channelConfig;
      this.messagesSent++;
      return true;
   }

   @Override
   public void connectMessageBroker() {
      this.connected = true;
   }

   @Override
   public void disconnectMessageBroker() {
      this.connected = false;
   }
}
