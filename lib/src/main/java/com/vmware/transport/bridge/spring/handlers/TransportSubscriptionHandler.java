package com.vmware.transport.bridge.spring.handlers;

import com.vmware.transport.bridge.spring.config.TransportBridgeConfiguration;
import com.vmware.transport.bridge.spring.services.TransportSubscriptionService;
import com.vmware.transport.core.util.TransportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Controller
public class TransportSubscriptionHandler implements ApplicationListener<SessionSubscribeEvent> {

    @Autowired
    private TransportSubscriptionService subService;

    @Autowired
    private TransportBridgeConfiguration transportBridgeConfiguration;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Listen to websocket client subscribed to topics events.
     *
     * Handles only subscription events for Transport destinations (destinations starting
     * with Transport-prefixes registered in the {@code TransportBridgeConfiguration}).
     */
    public void onApplicationEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String destination = sha.getDestination();

        String transportDestinationPrefix = TransportUtil.getTransportDestinationPrefix(
              transportBridgeConfiguration, destination);
        if (transportDestinationPrefix == null) {
            // Ignore events for non-Transport destinations.
            return;
        }

        String channel = TransportUtil.extractChannelName(transportBridgeConfiguration, destination);

        logger.info("[>] Transport Bridge: subscription requested: (" + channel + "), subId: " + sha.getSubscriptionId());
        subService.addSubscription(sha.getSubscriptionId(), sha.getSessionId(), channel, transportDestinationPrefix, event);
    }
}
