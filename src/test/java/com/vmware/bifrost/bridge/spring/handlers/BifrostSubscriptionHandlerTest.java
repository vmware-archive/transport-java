/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.handlers;

import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfiguration;
import com.vmware.bifrost.bridge.spring.services.BifrostSubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

public class BifrostSubscriptionHandlerTest {

    private BifrostBridgeConfiguration bifrostBridgeConfiguration;

    private BifrostSubscriptionService subService;

    private BifrostSubscriptionHandler handler;

    @Before
    public void before() {
        this.bifrostBridgeConfiguration = new BifrostBridgeConfiguration();

        this.bifrostBridgeConfiguration.addBifrostDestinationPrefixes("/topic1", "/topic2/");

        this.subService = Mockito.mock(BifrostSubscriptionService.class);

        this.handler = new BifrostSubscriptionHandler();

        Whitebox.setInternalState(this.handler, "subService", this.subService);
        Whitebox.setInternalState(this.handler, "bifrostBridgeConfiguration",
              this.bifrostBridgeConfiguration);
    }

    @Test
    public void testSubscriptionToNonBifrostDestination() {
        SessionSubscribeEvent event = createSessionSubscribeEvent("/non-bifrost/channel1");
        this.handler.onApplicationEvent(event);
        Mockito.verify(this.subService, Mockito.never()).addSubscription(
              Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testSubscriptionToBifrostDestination() {
        SessionSubscribeEvent event = createSessionSubscribeEvent("/topic1/channel1");
        this.handler.onApplicationEvent(event);
        Mockito.verify(this.subService).addSubscription(
              "subscriptionId", "sessionId", "channel1", "/topic1/", event);
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
