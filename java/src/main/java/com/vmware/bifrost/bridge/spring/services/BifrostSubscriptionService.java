package com.vmware.bifrost.bridge.spring.services;

import com.vmware.bifrost.bus.model.MessageObject;
import com.vmware.bifrost.bus.model.MessageType;
import com.vmware.bifrost.bus.model.MonitorObject;
import com.vmware.bifrost.bus.model.MonitorType;
import com.vmware.bifrost.core.util.Loggable;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service("bifrostSubscriptionService")
public class BifrostSubscriptionService extends Loggable implements BifrostBridgeSubscriptionRegistry {

    @Autowired
    private EventBus bus;

    @Autowired
    private SimpMessagingTemplate msgTmpl;

    private Map<String, BifrostSubscription> openSubscriptions;
    private Map<String, List<String>> sessionChannels;
    private Map<String, OpenChannel> openChannels;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public BifrostSubscriptionService() {
        openSubscriptions = new ConcurrentHashMap<>();
        openChannels = new ConcurrentHashMap<>();
        sessionChannels = new HashMap<>();
    }

    public Collection<BifrostSubscription> getSubscriptions() {
        return new ArrayList<>(openSubscriptions.values());
    }

    public Collection<String> getOpenChannels() {
        return new HashSet<>(openChannels.keySet());
    }

    public synchronized void addSubscription(
          String subId, String sessionId, String channelName,
          String destinationPrefix,
          SessionSubscribeEvent subscribeEvent) {

        BifrostSubscription subscription = new BifrostSubscription(channelName, subId, sessionId, destinationPrefix);
        if (openSubscriptions.containsKey(subscription.uniqueId)) {
            logger.info(String.format("[!] Bifröst Bus: subscription %s for channel %s already exists, ignoring",
                  subscription.uniqueId, channelName));
            return;
        }

        logger.info(String.format("[+] Bifröst Bus: creating channel subscription to '%s' subId: (%s)",
              channelName, subscription.uniqueId));

        if (!openChannels.containsKey(channelName)) {
            BusTransaction transaction = bus.listenStream(channelName,
                  (Message msg) -> {
                      this.logTraceMessage("Bifröst sending payload over socket: " + msg.getPayload().toString() + " to ", channelName);
                      msgTmpl.convertAndSend(destinationPrefix + channelName, msg.getPayload());
                  }
            );
            openChannels.put(channelName, new OpenChannel(channelName, transaction));
        }

        openSubscriptions.put(subscription.uniqueId, subscription);
        openChannels.get(channelName).activeSubscriptionsCount++;

        // check if this user has other channel subscriptions
        if (sessionChannels.containsKey(sessionId)) {
            sessionChannels.get(sessionId).add(channelName);
        } else {
            List<String> chanList = new ArrayList<>();
            chanList.add(channelName);
            sessionChannels.put(sessionId, chanList);
        }

        // Notify listeners that there is a new subscription to the channel.
        MonitorObject mo = new MonitorObject(
              MonitorType.MonitorNewBridgeSubscription, channelName, this.getClass().getName(),
              new NewBridgeSubscriptionEvent(subscription, subscribeEvent));
        this.bus.getApi().getMonitorStream().send(
              new MessageObject<>(MessageType.MessageTypeRequest, mo));
    }

    public synchronized void removeSubscription(String subId, String sessionId) {

        String uniqueSubId = BifrostSubscription.generateUniqueSubId(subId, sessionId);

        if (openSubscriptions.containsKey(uniqueSubId)) {
            BifrostSubscription sub = openSubscriptions.get(uniqueSubId);
            logger.info(String.format("[-] Bifröst Bus: unsubscribing from channel '%s' (%s)",
                  sub.channelName, sub.uniqueId));
            openSubscriptions.remove(uniqueSubId);
            onUnsubscribeFromChannel(sub.channelName);

            // remove from session mappings.
            if (sessionChannels.containsKey(sessionId)) {
                List<String> chans = sessionChannels.get(sessionId);
                chans.remove(sub.channelName);
            }
        }
    }

    public synchronized void unsubscribeSessionsAfterDisconnect(String sessionId) {
        if (sessionChannels.containsKey(sessionId)) {

            Collection<BifrostSubscription> subs = openSubscriptions.values();
            List<String> subscriptionsToRemove = new ArrayList<>();
            for (BifrostSubscription sub : subs) {
                if (sub.sessionId.equals(sessionId)) {
                    logger.info(String.format(
                          "[-] Bifröst Bus: closing subscription %s to channel '%s' after disconnect",
                          sub.uniqueId, sub.channelName));

                    // close subscription.
                    onUnsubscribeFromChannel(sub.channelName);
                    subscriptionsToRemove.add(sub.uniqueId);
                }
            }
            for (String subId : subscriptionsToRemove) {
                openSubscriptions.remove(subId);
            }

            sessionChannels.remove(sessionId);
        }
    }

    private void onUnsubscribeFromChannel(String channel) {
        OpenChannel openChannel = openChannels.get(channel);
        if (openChannel != null) {
            openChannel.activeSubscriptionsCount--;
            if (openChannel.activeSubscriptionsCount <= 0) {
                if (openChannel.transaction != null) {
                    openChannel.transaction.unsubscribe();
                }
                bus.closeChannel(channel, this.getClass().getName());
                openChannels.remove(channel);
            }
        }
    }

    private static class OpenChannel {

        public final String channelName;
        public final BusTransaction transaction;

        public int activeSubscriptionsCount;

        public OpenChannel(String channelName, BusTransaction transaction) {
            this.channelName = channelName;
            this.transaction = transaction;
            this.activeSubscriptionsCount = 0;
        }
    }

    public static class NewBridgeSubscriptionEvent {
        public final BifrostSubscription bifrostSubscription;
        public final SessionSubscribeEvent subscribeEvent;

        public NewBridgeSubscriptionEvent(
              BifrostSubscription subscription, SessionSubscribeEvent subscribeEvent) {
            this.bifrostSubscription = subscription;
            this.subscribeEvent = subscribeEvent;
        }
    }

    public static class BifrostSubscription {
        public final String channelName;
        public final String subId;
        public final String sessionId;
        public final String destinationPrefix;

        public final String uniqueId;

        public static String generateUniqueSubId(String subId, String sessionId) {
            return String.format("%s-%s", sessionId, subId);
        }

        public BifrostSubscription(String channelName, String subId, String sessionId, String destinationPrefix) {
            this.uniqueId = generateUniqueSubId(subId, sessionId);
            this.channelName = channelName;
            this.subId = subId;
            this.sessionId = sessionId;
            this.destinationPrefix = destinationPrefix;
        }
    }
}


