/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.handlers;

import com.vmware.bifrost.bridge.spring.services.BifrostSubscriptionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

public class BifrostDisconnectHandlerTest {

    private BifrostSubscriptionService subService;

    private BifrostDisconnectHandler handler;

    @Before
    public void before() {
        this.subService = Mockito.mock(BifrostSubscriptionService.class);

        this.handler = new BifrostDisconnectHandler();

        Whitebox.setInternalState(this.handler, "subService", this.subService);
    }

    @Test
    public void testSubscriptionToBifrostDestination() {
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
