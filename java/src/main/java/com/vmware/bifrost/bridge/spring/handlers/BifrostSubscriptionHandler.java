package com.vmware.bifrost.bridge.spring.handlers;

import com.vmware.bifrost.bridge.spring.services.BifrostSubscriptionService;
import com.vmware.bifrost.bridge.util.BifrostUtil;
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
public class BifrostSubscriptionHandler implements ApplicationListener<SessionSubscribeEvent> {

    @Autowired
    private BifrostSubscriptionService subService;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String channel =  BifrostUtil.convertTopicToChannel(sha.getDestination());

        logger.info("[>] Bifröst Bridge: subscription requested: (" + channel + "), subId: " + sha.getSubscriptionId());
        subService.addSubscription(sha.getSubscriptionId(), sha.getSessionId(), channel);
    }
}
