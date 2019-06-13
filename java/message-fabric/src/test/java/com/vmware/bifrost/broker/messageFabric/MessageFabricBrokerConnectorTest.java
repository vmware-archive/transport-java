/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.broker.messageFabric;

import com.vmware.atlas.message.client.model.enums.MessageType;
import com.vmware.atlas.message.client.service.MessageClient;
import com.vmware.bifrost.broker.GalacticMessageHandler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Serializable;

public class MessageFabricBrokerConnectorTest {

   private Object galacticMessage;
   private int counter = 0;
   private int errorCounter = 0;
   private GalacticMessageHandler handler;

   private MessageClient messageClient;

   @Before
   public void before() {
      this.galacticMessage = null;
      this.counter = 0;
      this.messageClient = Mockito.mock(MessageClient.class);
      this.handler = new GalacticMessageHandler() {
         @Override
         public void onMessage(Object o) {
            galacticMessage = o;
            counter++;
         }

         @Override
         public void onError(Object o) {
            errorCounter++;
         }
      };
   }

   @Test
   public void testCreate() {
      MessageFabricBrokerConnector mfBrokerConnnector = new MessageFabricBrokerConnector(messageClient);
      Assert.assertNotNull(mfBrokerConnnector.getMessageBrokerId());

      mfBrokerConnnector = new MessageFabricBrokerConnector("broker-id", messageClient);
      Assert.assertEquals("broker-id", mfBrokerConnnector.getMessageBrokerId());
      mfBrokerConnnector.connectMessageBroker();
   }

   @Test
   public void testConnectAndDisconnect() {
      MessageFabricBrokerConnector mfBrokerConnector = new MessageFabricBrokerConnector(messageClient);

      mfBrokerConnector.connectMessageBroker();
      Mockito.verify(this.messageClient, Mockito.times(1)).connect();

      mfBrokerConnector.disconnectMessageBroker();
      Mockito.verify(this.messageClient, Mockito.times(1)).disconnect();
   }

   @Test
   public void testSendMessageInvalidArgs() {

      MessageFabricBrokerConnector mfBrokerConnector = new MessageFabricBrokerConnector(messageClient);
      Assert.assertFalse(mfBrokerConnector.sendMessage(null, "test"));
      Assert.assertFalse(mfBrokerConnector.sendMessage(
            new MessageFabricChannelConfig("id", null, MessageType.MESSAGE, "test", true), "test"));
      Assert.assertFalse(mfBrokerConnector.sendMessage(
            new MessageFabricChannelConfig("id", String.class, MessageType.MESSAGE, "", true), "test"));
      Assert.assertFalse(mfBrokerConnector.sendMessage(
            new MessageFabricChannelConfig("id", String.class, MessageType.MESSAGE, null, true), "test"));
      Assert.assertFalse(mfBrokerConnector.sendMessage(
            new MessageFabricChannelConfig(
                  "id", String.class, MessageType.MESSAGE, "test", true), new Object()));
      Assert.assertFalse(mfBrokerConnector.sendMessage(
            new MessageFabricChannelConfig("id", String.class, MessageType.MESSAGE, "test", true), null));
   }

   @Test
   public void testSendMessage() {
      MessageFabricBrokerConnector mfBrokerConnector = new MessageFabricBrokerConnector(messageClient);
      mfBrokerConnector.sendMessage(
            mfBrokerConnector.newDirectMessageFabricChannelConfig(
                  MessageType.MESSAGE, "test-service", String.class),
            "test-message");
      Mockito.verify(this.messageClient, Mockito.times(1)).sendDirectAsync(
            MessageType.MESSAGE, "test-service", "test-message");

      CustomMessage testMessage = new CustomMessage();

      mfBrokerConnector.sendMessage(
            mfBrokerConnector.newTopicMessageFabricChannelConfig(
                  MessageType.EVENT, "test-topic", CustomMessage.class),
            testMessage);
      Mockito.verify(this.messageClient, Mockito.times(1)).sendAsync(
            MessageType.EVENT, "test-topic", testMessage);
   }

   @Test
   public void testSubscribeInvalidArgs() {
      MessageFabricBrokerConnector mfBrokerConnector = new MessageFabricBrokerConnector(messageClient);
      Assert.assertNull(mfBrokerConnector.subscribeToChannel(null, null));
      Assert.assertNull(mfBrokerConnector.subscribeToChannel(null, handler));
      Assert.assertNull(mfBrokerConnector.subscribeToChannel(
            new MessageFabricChannelConfig("id", String.class, MessageType.MESSAGE, "", true), handler));

      Assert.assertNull(mfBrokerConnector.subscribeToChannel(
            new MessageFabricChannelConfig("id", String.class, MessageType.MESSAGE, null, true), handler));

      Assert.assertNull(mfBrokerConnector.subscribeToChannel(
            new MessageFabricChannelConfig("id", String.class, MessageType.MESSAGE, "test", true), null));
   }

   @Test
   public void testSubscribe() {
      MessageFabricBrokerConnector mfBrokerConnector = new MessageFabricBrokerConnector(messageClient);
      MessageFabricChannelConfig directGalacticChannelConf =
            mfBrokerConnector.newDirectMessageFabricChannelConfig(
                  MessageType.MESSAGE, "test-service", String.class);

      MessageFabricSubscription sub = mfBrokerConnector.subscribeToChannel(
            directGalacticChannelConf, handler);

      Mockito.verify(this.messageClient, Mockito.never()).subscribe(Mockito.anyString());
      Mockito.verify(this.messageClient).addListeners(sub.listener);
      Assert.assertEquals(sub.channelConfig, directGalacticChannelConf);

      sub.listener.onMessage("test-message");
      Assert.assertEquals(galacticMessage, "test-message");

      MessageFabricChannelConfig topicGalacticChannelConfig =
            mfBrokerConnector.newTopicMessageFabricChannelConfig(
                  MessageType.MESSAGE, "test-topic", CustomMessage.class);

      MessageFabricSubscription sub2 = mfBrokerConnector.subscribeToChannel(
            topicGalacticChannelConfig, handler);

      Mockito.verify(this.messageClient, Mockito.times(1)).subscribe("test-topic");
      Mockito.verify(this.messageClient).addListeners(sub2.listener);
      Assert.assertEquals(sub2.channelConfig, topicGalacticChannelConfig);

      MessageFabricSubscription sub3 = mfBrokerConnector.subscribeToChannel(
            topicGalacticChannelConfig, handler);

      Mockito.verify(this.messageClient, Mockito.times(1)).subscribe("test-topic");
      Mockito.verify(this.messageClient).addListeners(sub3.listener);
   }

   @Test
   public void testSubscribeWithError() {
      MessageFabricBrokerConnector mfBrokerConnector = new MessageFabricBrokerConnector(messageClient);
      MessageFabricChannelConfig topicGalacticChannelConfig =
            mfBrokerConnector.newTopicMessageFabricChannelConfig(
                  MessageType.MESSAGE, "test-topic", CustomMessage.class);

      Mockito.doThrow(new RuntimeException("first-subscribe-fails")) // first call throws
            .doNothing() // second call is ok
            .when(messageClient).subscribe("test-topic");


      Assert.assertNull(mfBrokerConnector.subscribeToChannel(topicGalacticChannelConfig, handler));
      MessageFabricSubscription sub =
            mfBrokerConnector.subscribeToChannel(topicGalacticChannelConfig, handler);
      Assert.assertNotNull(sub);

      mfBrokerConnector.unsubscribeFromChannel(sub);
      Mockito.verify(this.messageClient, Mockito.times(1)).unsubscribe("test-topic");
   }


   @Test
   public void testUnsubscribe() {

      MessageFabricBrokerConnector mfBrokerConnector = new MessageFabricBrokerConnector(messageClient);

      Assert.assertFalse(mfBrokerConnector.unsubscribeFromChannel(null));

      MessageFabricChannelConfig directGalacticChannelConf =
            mfBrokerConnector.newDirectMessageFabricChannelConfig(
                  MessageType.MESSAGE, "test-service", String.class);

      MessageFabricChannelConfig topicGalacticChannelConfig =
            mfBrokerConnector.newTopicMessageFabricChannelConfig(
                  MessageType.MESSAGE, "test-topic", CustomMessage.class);

      MessageFabricSubscription sub =
            mfBrokerConnector.subscribeToChannel(directGalacticChannelConf, handler);

      Assert.assertTrue(mfBrokerConnector.unsubscribeFromChannel(sub));
      Assert.assertNull(sub.listener);
      Assert.assertFalse(mfBrokerConnector.unsubscribeFromChannel(sub));

      MessageFabricSubscription sub2 =
            mfBrokerConnector.subscribeToChannel(topicGalacticChannelConfig, handler);
      MessageFabricSubscription sub3 =
            mfBrokerConnector.subscribeToChannel(topicGalacticChannelConfig, handler);

      mfBrokerConnector.unsubscribeFromChannel(sub2);
      Mockito.verify(messageClient, Mockito.never()).unsubscribe("test-topic");
      mfBrokerConnector.unsubscribeFromChannel(sub3);
      Mockito.verify(messageClient, Mockito.times(1)).unsubscribe("test-topic");
   }

   private static class CustomMessage implements Serializable {
      public CustomMessage() {}
   }
}
