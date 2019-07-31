package com.vmware.bifrost.bridge.spring.handlers;

import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfiguration;
import com.vmware.bifrost.bridge.spring.services.BifrostSubscriptionService;
import com.vmware.bifrost.core.util.BifrostUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Controller
public class BifrostSubscriptionHandler implements ApplicationListener<SessionSubscribeEvent> {

    @Autowired
    private BifrostSubscriptionService subService;

    @Autowired
    private BifrostBridgeConfiguration bifrostBridgeConfiguration;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Listen to websocket client subscribed to topics events.
     *
     * Handles only subscription events for bifrost destinations (destinations starting
     * with bifrost-prefixes registered in the {@code BifrostBridgeConfiguration}).
     */
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getDestination();

        String bifrostDestinationPrefix = BifrostUtil.getBifrostDestinationPrefix(
              bifrostBridgeConfiguration, destination);
        if (bifrostDestinationPrefix == null) {
            // Ignore events for non-bifrost destinations.
            return;
        }

        String channel = BifrostUtil.extractChannelName(bifrostBridgeConfiguration, destination);

        logger.info("[>] Bifrost Bridge: subscription requested: (" + channel + "), subId: " + sha.getSubscriptionId());
        subService.addSubscription(sha.getSubscriptionId(), sha.getSessionId(), channel, bifrostDestinationPrefix, event);
    }
}
