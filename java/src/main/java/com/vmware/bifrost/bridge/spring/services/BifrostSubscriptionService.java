package com.vmware.bifrost.bridge.spring.services;

import com.vmware.bifrost.bridge.spring.handlers.BifrostSubscriptionHandler;
import com.vmware.bifrost.bridge.util.BifrostUtil;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bus.MessageHandler;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BifrostSubscriptionService {

    @Autowired
    private MessagebusService bus;

    @Autowired
    private SimpMessagingTemplate msgTmpl;

    private Map<String, BifrostSubscription> openSubscriptions;
    private List<String> openChannels;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    BifrostSubscriptionService() {
        openSubscriptions = new HashMap<>();
        openChannels = new ArrayList<>();
    }

    public Map<String, BifrostSubscription> getSubscriptions() {
        return openSubscriptions;
    }

    public List<String> getOpenChannels() {
        return openChannels;
    }

    public void addSubscription(String subId, String channelName) {

        if (!openChannels.contains(channelName)) {

            logger.info("[+] Bifröst Bus: creating channel subscription to '" + channelName + "' subId: (" + subId + ")");

            BusTransaction transaction = bus.listenStream(channelName,
                    (Message msg) -> {
                        logger.debug("Bifröst sending payload: " + msg.getPayload().toString() + " to " + channelName);
                        msgTmpl.convertAndSend(BifrostUtil.convertChannelToTopic(channelName), msg.getPayload());
                    }
            );

            openSubscriptions.put(subId, new BifrostSubscription(channelName, subId, transaction));
            openChannels.add(channelName);

        } else {
            logger.info("[!] Bifröst Bus: subscription " + channelName + " already exists, ignoring");
        }
    }

    public void removeSubscription(String subId) {
        BifrostSubscription sub;

        if (openSubscriptions.containsKey(subId)) {

            sub = openSubscriptions.get(subId);
            logger.info("[-] Bifröst Bus: unsubscribing from channel '" + sub.channelName + "' (" + sub.subId + ")");

            sub.transaction.unsubscribe();
            openSubscriptions.remove(subId);
            openChannels.remove(sub.channelName);
        }
    }
}

class BifrostSubscription {
    public String channelName;
    public String subId;
    public BusTransaction transaction;

    BifrostSubscription(String channelName, String subId, BusTransaction transaction) {
        this.channelName = channelName;
        this.subId = subId;
        this.transaction = transaction;
    }
}
