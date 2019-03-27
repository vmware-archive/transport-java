/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.services;

import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.EventBusImpl;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MonitorObject;
import com.vmware.bifrost.bus.model.MonitorType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        EventBusImpl.class,
        BifrostSubscriptionService.class,
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BifrostSubscriptionServiceTest {

    @Autowired
    private EventBus bus;

    @Autowired
    private BifrostSubscriptionService subscriptionService;

    @MockBean
    private SimpMessagingTemplate msgTmpl;

    private String channel;
    private String channel2;
    private String destinationPrefix;
    private SessionSubscribeEvent subscribeEvent1;
    private SessionSubscribeEvent subscribeEvent2;

    private MonitorObject monitorObject;
    private int count;

    @Before
    public void before() {
        this.channel = "local-channel";
        this.channel2 = "local-channel2";
        this.destinationPrefix = "/destinationPrefix/";
        this.monitorObject = null;
        this.count = 0;
        this.subscribeEvent1 = createSessionSubscribeEvent(this.destinationPrefix + this.channel);
        this.subscribeEvent2 = createSessionSubscribeEvent(this.destinationPrefix + this.channel2);
    }

    @Test
    public void testAddSubscriptionWithResponse() {

        this.bus.getApi().getMonitor().subscribe(message -> {
            MonitorObject mo = (MonitorObject) message.getPayload();
            if (mo.getType() == MonitorType.MonitorNewBridgeSubscription) {
                this.monitorObject = mo;
            }
        });

        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);

        Assert.assertTrue(bus.getApi().getChannelMap().containsKey(this.channel));
        Assert.assertEquals(bus.getApi().getChannelMap().get(this.channel).getRefCount().intValue(), 1);
        Assert.assertTrue(subscriptionService.getOpenChannels().contains(this.channel));

        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 1);
        BifrostSubscriptionService.BifrostSubscription sub =
              subscriptionService.getSubscriptions().iterator().next();

        Assert.assertEquals(sub.subId, "sub1");
        Assert.assertEquals(sub.sessionId, "session1");
        Assert.assertEquals(sub.channelName, this.channel);

        Response<String> response = new Response<>(Integer.valueOf(1), UUID.randomUUID(), false);
        response.setPayload("response1");

        bus.sendResponseMessage(this.channel, response);
        Mockito.verify(msgTmpl).convertAndSend(this.destinationPrefix + this.channel, response);

        Assert.assertNotNull(this.monitorObject);
        BifrostSubscriptionService.NewBridgeSubscriptionEvent subscriptionEvent =
              (BifrostSubscriptionService.NewBridgeSubscriptionEvent) this.monitorObject.getData();
        Assert.assertNotNull(subscriptionEvent);
        Assert.assertEquals(subscriptionEvent.bifrostSubscription.channelName, this.channel);
        Assert.assertEquals(subscriptionEvent.subscribeEvent, this.subscribeEvent1);
    }

    @Test
    public void testAddSubscriptionWithMultipleSubscriptions() {

        this.bus.getApi().getMonitor().subscribe(message -> {
            MonitorObject mo = (MonitorObject) message.getPayload();
            if (mo.getType() == MonitorType.MonitorNewBridgeSubscription &&
                  mo.getChannel().equals(this.channel)) {
                this.count++;
            }
        });

        // Verify that we can add subscriptions from different sessions with the same subscriptionId
        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);
        subscriptionService.addSubscription("sub1", "session2", this.channel, this.destinationPrefix, this.subscribeEvent1);
        subscriptionService.addSubscription("sub2", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);

        Assert.assertTrue(bus.getApi().getChannelMap().containsKey(this.channel));
        Assert.assertEquals(bus.getApi().getChannelMap().get(this.channel).getRefCount().intValue(), 1);

        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 1);
        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 3);

        bus.sendResponseMessage(this.channel, "response1");
        Mockito.verify(msgTmpl, Mockito.times(1)).convertAndSend(
              this.destinationPrefix + this.channel, "response1");

        subscriptionService.removeSubscription("sub1", "session1");
        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 2);

        // Verify that we have one MonitorNewBridgeSubscription event for each
        // subscription.
        Assert.assertEquals(this.count, 3);
    }

    @Test
    public void testConcurrentOpenChannelIterator() {

        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);
        subscriptionService.addSubscription("sub2", "session1", this.channel2, this.destinationPrefix, this.subscribeEvent2);

        Iterator<String> oc = subscriptionService.getOpenChannels().iterator();
        oc.next();
        Assert.assertEquals(oc.hasNext(), true);

        // Remove all subscriptions for session1
        subscriptionService.unsubscribeSessionsAfterDisconnect("session1");
        oc.next();
    }

   @Test
   public void testGetOpenChannelsWithAttribute() {

      subscriptionService.addSubscription("sub1", "session1", "chan1", this.destinationPrefix, this.subscribeEvent1);
      bus.getApi().setChannelAttribute("chan1", "clientId", "client1");

      subscriptionService.addSubscription("sub2", "session1", "chan2", this.destinationPrefix, this.subscribeEvent2);
      bus.getApi().setChannelAttribute("chan2", "clientId", "client2");

      subscriptionService.addSubscription("sub3", "session1", "chan3", this.destinationPrefix, this.subscribeEvent2);
      bus.getApi().setChannelAttribute("chan3", "clientId", "client1");

      subscriptionService.addSubscription("sub4", "session2", "chan1", this.destinationPrefix, this.subscribeEvent2);

      subscriptionService.addSubscription("sub5", "session2", "chan4", this.destinationPrefix, this.subscribeEvent2);
      bus.getApi().setChannelAttribute("chan4", "clientId", "client3");

      Assert.assertTrue(subscriptionService.getOpenChannelsWithAttribute("invalid-attr", "value").isEmpty());
      Assert.assertTrue(subscriptionService.getOpenChannelsWithAttribute(null, "value").isEmpty());
      Assert.assertTrue(subscriptionService.getOpenChannelsWithAttribute("clientId", null).isEmpty());

      Collection<String> chansForClient1 = subscriptionService.getOpenChannelsWithAttribute("clientId", "client1");
      Assert.assertEquals(chansForClient1.size(), 2);
      Assert.assertTrue(chansForClient1.contains("chan1"));
      Assert.assertTrue(chansForClient1.contains("chan3"));

      Collection<String> chansForClient3 = subscriptionService.getOpenChannelsWithAttribute("clientId", "client3");
      Assert.assertEquals(chansForClient3.size(), 1);
      Assert.assertTrue(chansForClient3.contains("chan4"));

      // Remove all subscriptions for session1
      subscriptionService.unsubscribeSessionsAfterDisconnect("session2");
      Assert.assertTrue(subscriptionService.getOpenChannelsWithAttribute("clientId", "client3").isEmpty());

      Assert.assertEquals(subscriptionService.getOpenChannelsWithAttribute("clientId", "client1").size(), 2);
   }

    @Test
    public void testRemoveLastSubscriptionToChannel() {

        this.bus.listenRequestStream(this.channel, (Message m) ->  {});
        this.bus.listenRequestStream(this.channel2, (Message m) ->  {});

        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);
        subscriptionService.addSubscription("sub2", "session1", this.channel2, this.destinationPrefix, this.subscribeEvent2);
        subscriptionService.addSubscription("sub3", "session2", this.channel, this.destinationPrefix, this.subscribeEvent1);

        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 3);
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 2);

        // Remove all subscriptions for session1
        subscriptionService.unsubscribeSessionsAfterDisconnect("session1");

        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 1);
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 1);
        Assert.assertTrue(subscriptionService.getOpenChannels().contains(this.channel));

        // Send a response to channel2 and verify that it's ignored since
        // there are no active subscriptions for channel2.
        bus.sendResponseMessage(this.channel2, "channel2-response1");
        Mockito.verify(msgTmpl, Mockito.never()).convertAndSend(
              this.destinationPrefix + this.channel2, "channel2-response1");

        // Send response to first channel and verify that it's sent back via
        // the messaging template.
        bus.sendResponseMessage(this.channel, "channel-response1");
        Mockito.verify(msgTmpl, Mockito.times(1)).convertAndSend(
              this.destinationPrefix + this.channel, "channel-response1");

        // Remove last subscription to first channel
        subscriptionService.removeSubscription("sub3", "session2");
        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 0);
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 0);

        // Verify that reponses to first channel are ignored.
        bus.sendResponseMessage(this.channel, "channel-response1");
        Mockito.verify(msgTmpl, Mockito.times(1)).convertAndSend(
              this.destinationPrefix + this.channel, "channel-response1");

        // Verify that we can re-subscribe successfully to first channel
        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);

        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 1);
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 1);
        // Verify that responses are processed correctly after the re-subscription.
        bus.sendResponseMessage(this.channel, "channel-response1");
        Mockito.verify(msgTmpl, Mockito.times(2)).convertAndSend(
              this.destinationPrefix + this.channel, "channel-response1");
    }

    @Test
    public void testRemoveSubscriptionWithInvalidSubId() {
        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 1);
        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 1);

        subscriptionService.removeSubscription("sub1", "session2");
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 1);
        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 1);
    }

    @Test
    public void testAddSubscriptionWithAlreadySubscribedSubId() {
        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);
        subscriptionService.addSubscription("sub1", "session1", this.channel, this.destinationPrefix, this.subscribeEvent1);
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 1);
        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 1);

        subscriptionService.removeSubscription("sub1", "session1");
        Assert.assertEquals(subscriptionService.getOpenChannels().size(), 0);
        Assert.assertEquals(subscriptionService.getSubscriptions().size(), 0);
    }

    private SessionSubscribeEvent createSessionSubscribeEvent(String destination) {
        MessageBuilder<byte[]> messageBuilder = MessageBuilder.withPayload(new byte[0]);
        messageBuilder.setHeader("simpDestination", destination)
              .setHeader("simpSubscriptionId", "subscriptionId")
              .setHeader("simpSessionId", "sessionId")
              .setHeader("stompCommand", StompCommand.SUBSCRIBE);

        return new SessionSubscribeEvent(new Object(), messageBuilder.build());
    }
}
