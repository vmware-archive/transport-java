package com.vmware.bifrost.bridge.spring;

import com.vmware.bifrost.bus.MessagebusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Controller
public class BifrostSubscriptionManager implements ApplicationListener<SessionSubscribeEvent> {

    @Autowired
    private MessagebusService bus;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Logger logger = LoggerFactory.getLogger(BifrostSubscriptionManager.class);

    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());




        System.out.println("Subscribed Event [sessionId: " + sha.getSessionId());
        System.out.println("Subscribed Event [event: " + sha.getDestination());
    }
}
