/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.handlers;

import com.vmware.transport.bridge.spring.services.TransportSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Controller
public class TransportDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {

    @Autowired
    private TransportSubscriptionService subService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void onApplicationEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("[x] Transport Bridge: disconnect:" + sha.getSessionId());
        subService.unsubscribeSessionsAfterDisconnect(sha.getSessionId());
    }

}
