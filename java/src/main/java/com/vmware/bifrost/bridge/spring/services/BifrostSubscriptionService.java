package com.vmware.bifrost.bridge.spring.services;

import com.vmware.bifrost.core.util.Loggable;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BifrostSubscriptionService extends Loggable {

    @Autowired
    private EventBus bus;

    @Autowired
    private SimpMessagingTemplate msgTmpl;

    private Map<String, BifrostSubscription> openSubscriptions;
    private Map<String, List<String>> sessionChannels;
    private List<String> openChannels;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    BifrostSubscriptionService() {
        openSubscriptions = new HashMap<>();
        openChannels = new ArrayList<>();
        sessionChannels = new HashMap<>();
    }

    public Map<String, BifrostSubscription> getSubscriptions() {
        return openSubscriptions;
    }

    public List<String> getOpenChannels() {
        return openChannels;
    }

    public void addSubscription(String subId, String sessionId, String channelName, String destinationPrefix) {

        if (!openChannels.contains(channelName)) {

            logger.info("[+] Bifröst Bus: creating channel subscription to '" + channelName + "' subId: (" + subId + ")");


            BusTransaction transaction = bus.listenStream(channelName,
                    (Message msg) -> {
                        this.logDebugMessage("Bifröst sending payload over socket: " + msg.getPayload().toString() + " to ", channelName);
                        msgTmpl.convertAndSend(destinationPrefix + channelName, msg.getPayload());
                    }

            );



            openSubscriptions.put(subId, new BifrostSubscription(channelName, subId, sessionId, transaction));
            openChannels.add(channelName);

            // check if this user has other channel subscriptions
            if (sessionChannels.containsKey(sessionId)) {
                sessionChannels.get(sessionId).add(channelName);
            } else {
                List<String> chanList = new ArrayList<>();
                chanList.add(channelName);
                sessionChannels.put(sessionId, chanList);
            }

        } else {
            logger.info("[!] Bifröst Bus: subscription " + channelName + " already exists, ignoring");
        }
    }

    public void removeSubscription(String subId, String sessionId) {
        BifrostSubscription sub;

        if (openSubscriptions.containsKey(subId)) {

            sub = openSubscriptions.get(subId);
            logger.info("[-] Bifröst Bus: unsubscribing from channel '" + sub.channelName + "' (" + sub.subId + ")");

            sub.transaction.unsubscribe();
            openSubscriptions.remove(subId);
            openChannels.remove(sub.channelName);

            // remove from session mappings.
            if (sessionChannels.containsKey(sessionId)) {
                List<String> chans = sessionChannels.get(sessionId);
                if (chans.contains(sub.channelName)) {
                    chans.remove(sub.channelName);
                }
            }
        }
    }

    public void unsubsribeSessionsAfterDisconnect(String sessionId) {

        if (sessionChannels.containsKey(sessionId)) {
            List<String> chans = sessionChannels.get(sessionId);

            for (String chan : chans) {

                // close subscription.
                bus.closeChannel(chan, this.getClass().getName());
                openChannels.remove(chan);

                Collection<BifrostSubscription> subs = openSubscriptions.values();
                List<String> removeSubs = new ArrayList<>();
                for (BifrostSubscription sub : subs) {
                    if (sub.sessionId.equals(sessionId)) {
                        sub.transaction.unsubscribe();
                        removeSubs.add(sub.subId);
                    }
                }
                for (String subId : removeSubs) {
                    openSubscriptions.remove(subId);
                }

                logger.info("[-] Bifröst Bus: closing subscription to channel '" + chan + "' after disconnect");
            }
        }
    }
}

class BifrostSubscription {
    public String channelName;
    public String subId;
    public String sessionId;
    public BusTransaction transaction;

    BifrostSubscription(String channelName, String subId, String sessionId, BusTransaction transaction) {
        this.channelName = channelName;
        this.subId = subId;
        this.transaction = transaction;
        this.sessionId = sessionId;
    }
}
