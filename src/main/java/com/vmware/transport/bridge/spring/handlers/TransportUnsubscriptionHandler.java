package com.vmware.transport.bridge.spring.handlers;

import com.vmware.transport.bridge.spring.services.TransportSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

@Controller
public class TransportUnsubscriptionHandler implements ApplicationListener<SessionUnsubscribeEvent> {

    @Autowired
    private TransportSubscriptionService subService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void onApplicationEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        logger.info("[<] Transport Bridge: unsubscribing subId:" + sha.getSubscriptionId());
        subService.removeSubscription(sha.getSubscriptionId(), sha.getSessionId());
    }
}
