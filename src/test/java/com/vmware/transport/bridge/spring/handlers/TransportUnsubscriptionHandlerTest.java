/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.handlers;

import com.vmware.transport.bridge.spring.services.TransportSubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

public class TransportUnsubscriptionHandlerTest {

    private TransportSubscriptionService subService;

    private TransportUnsubscriptionHandler handler;

    @Before
    public void before() {
        this.subService = Mockito.mock(TransportSubscriptionService.class);

        this.handler = new TransportUnsubscriptionHandler();

        Whitebox.setInternalState(this.handler, "subService", this.subService);
    }

    @Test
    public void testSubscriptionToTransportDestination() {
        this.handler.onApplicationEvent(createSessionUnsubscribeEvent("subscriptionId", "sessionId"));
        Mockito.verify(this.subService).removeSubscription(
              "subscriptionId", "sessionId");
    }

    private SessionUnsubscribeEvent createSessionUnsubscribeEvent(
          String subscriptionId, String sessionId) {

        MessageBuilder<byte[]> messageBuilder = MessageBuilder.withPayload(new byte[0]);
        messageBuilder.setHeader("simpSubscriptionId", subscriptionId)
              .setHeader("simpSessionId", sessionId);

        return new SessionUnsubscribeEvent(new Object(), messageBuilder.build());
    }
}
