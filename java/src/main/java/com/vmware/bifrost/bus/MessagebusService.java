package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.*;
import io.reactivex.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Observable;

import java.util.HashMap;
import java.util.Map;


/**
 * Copyright(c) VMware Inc. 2017
 */

public class MessagebusService {

    private Map<String, Channel> channelMap;
    private Channel monitorStream;
    private String monitorChannel;
    private boolean dumpMonitor;
    private Logger logger;

    MessagebusService () {

        logger = LoggerFactory.getLogger(MessagebusService.class);
        this.channelMap = new HashMap<String, Channel>();
        this.monitorChannel = MonitorChannel.stream;
        this.monitorStream = new Channel(this.monitorChannel);
        this.channelMap.put(this.monitorChannel, this.monitorStream);

        this.enableMonitorDump(true);
    }

    public boolean enableMonitorDump(boolean flag) {
        this.dumpMonitor = flag;
        return this.dumpMonitor;
    }

    public void increment(String channelName) {
        this.channelMap.get(channelName).increment();
    }

    public Map<String, Channel> getChannelMap() {
        return this.channelMap;
    }

    public Subject<Message> getMonitor() {
        return this.monitorStream.getStreamObject();
    }

    public boolean isLoggingEnabled() {
        return this.dumpMonitor;
    }

    public Channel getChannelObject(String cname, String from) {
        Channel channel;
        String symbol = " + ";

        if (this.channelMap.containsKey(cname)) {
            channel = this.channelMap.get(cname);
        } else {
            channel = new Channel(cname);
            this.channelMap.put(cname, channel);
            symbol = " +++ ";
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorNewChannel, cname, from, symbol);
        this.monitorStream.send(new Message<MonitorObject>(MessageType.MessageTypeRequest, mo));
        channel.increment();
        return channel;
    }

    public Observable<Message> getChannel(String channelName, String from) {
        return this.getChannelObject(channelName, from)
                .getStreamObject()
                .map(
                        (Message m) -> {
                            this.logger.info("ooooo bummy", m);
                            return m;
                        });
    }

    public void send(String cname, Message message, String from) {
        // TEMPORARY - flag all messages without schema
        if (message.getSchema() == null) {
            this.logger.warn("* No schema in message to " + cname, from);
        }
        MonitorObject mo;

        if (!this.channelMap.containsKey(cname)) {
            mo = new MonitorObject(MonitorType.MonitorDropped, cname, from, message);
            this.monitorStream.send(new Message<MonitorObject>(MessageType.MessageTypeRequest, mo));
            return;
        }

        mo = new MonitorObject(MonitorType.MonitorData, cname, from, message);
        this.monitorStream.send(new Message<MonitorObject>(MessageType.MessageTypeRequest, mo));
        this.channelMap.get(cname).send(message);

    }

//    public void sendRequest(String cname, Object payload): boolean {
//        let mh: MessageHandlerConfig = new MessageHandlerConfig(cname, payload, true, cname);
//        this.send(mh.sendChannel, new Message().request(mh, schema), name);
//    }


}
