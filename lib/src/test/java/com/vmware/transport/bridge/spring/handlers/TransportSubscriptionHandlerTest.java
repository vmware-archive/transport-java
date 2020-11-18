/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.handlers;

import com.vmware.transport.bridge.spring.config.TransportBridgeConfiguration;
import com.vmware.transport.bridge.spring.services.TransportSubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

public class TransportSubscriptionHandlerTest {

    private TransportBridgeConfiguration transportBridgeConfiguration;

    private TransportSubscriptionService subService;

    private TransportSubscriptionHandler handler;

    @Before
    public void before() {
        this.transportBridgeConfiguration = new TransportBridgeConfiguration();

        this.transportBridgeConfiguration.addTransportDestinationPrefixes("/topic1", "/topic2/");

        this.subService = Mockito.mock(TransportSubscriptionService.class);

        this.handler = new TransportSubscriptionHandler();

        Whitebox.setInternalState(this.handler, "subService", this.subService);
        Whitebox.setInternalState(this.handler, "transportBridgeConfiguration",
              this.transportBridgeConfiguration);
    }

    @Test
    public void testSubscriptionToNonTransportDestination() {
        SessionSubscribeEvent event = createSessionSubscribeEvent("/non-transport/channel1");
        this.handler.onApplicationEvent(event);
        Mockito.verify(this.subService, Mockito.never()).addSubscription(
              Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testSubscriptionToTransportDestination() {
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
