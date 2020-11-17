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
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

public class TransportDisconnectHandlerTest {

    private TransportSubscriptionService subService;

    private TransportDisconnectHandler handler;

    @Before
    public void before() {
        this.subService = Mockito.mock(TransportSubscriptionService.class);

        this.handler = new TransportDisconnectHandler();

        Whitebox.setInternalState(this.handler, "subService", this.subService);
    }

    @Test
    public void testSubscriptionToTransportDestination() {
        this.handler.onApplicationEvent(createDisconnectEvent("sessionId"));
        Mockito.verify(this.subService).unsubscribeSessionsAfterDisconnect("sessionId");
    }

    private SessionDisconnectEvent createDisconnectEvent(String sessionId) {
        MessageBuilder<byte[]> messageBuilder = MessageBuilder.withPayload(new byte[0]);
        messageBuilder.setHeader("simpSessionId", sessionId);
        return new SessionDisconnectEvent(
              new Object(), messageBuilder.build(), sessionId, CloseStatus.NORMAL);
    }
}
