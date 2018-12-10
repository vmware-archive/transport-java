/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus;

import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.bus.model.Channel;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObject;
import com.vmware.bifrost.bus.model.MessageType;
import com.vmware.bifrost.bus.model.MonitorChannel;
import com.vmware.bifrost.bus.model.MonitorObject;
import com.vmware.bifrost.bus.model.MonitorType;
import io.reactivex.Observable;
import io.reactivex.subjects.Subject;

import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unchecked")
public class EventBusLowApiImpl extends Loggable implements EventBusLowApi {

    private final Map<String, Channel> internalChannelMap;

    private Channel monitorStream;
    private String monitorChannel;

    private boolean dumpMonitor;

    public EventBusLowApiImpl(Map<String, Channel> channelMap) {
        this.internalChannelMap = channelMap;

        this.monitorChannel = MonitorChannel.stream;
        this.monitorStream = new Channel(this.monitorChannel);
    }

    @Override
    public boolean enableMonitorDump(boolean flag) {
        this.dumpMonitor = flag;
        return this.dumpMonitor;
    }

    @Override
    public boolean isLoggingEnabled() {
        return this.dumpMonitor;
    }

    @Override
    public Map<String, Channel> getChannelMap() {
        return Collections.unmodifiableMap(this.internalChannelMap);
    }

    @Override
    public Subject<Message> getMonitor() {
        return this.monitorStream.getStreamObject();
    }

    @Override
    public void close(String cname, String from) {
        if (!this.internalChannelMap.containsKey(cname)) {
            return;
        }

        Channel channel = this.internalChannelMap.get(cname);
        channel.decrement();
        MonitorObject mo = new MonitorObject(
              MonitorType.MonitorCloseChannel, cname, from,
              "close [" + cname.trim() + "] " + channel.getRefCount() + " references remaining");

        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));

        if (channel.getRefCount() == 0) {
            this.complete(channel, from);
        }
    }

    @Override
    public void complete(String channel, String from) {
        Channel chan = this.getChannelObject(channel, from);
        this.complete(chan, from);
    }

    @Override
    public void complete(Channel channel, String from) {

        MonitorObject mo = new MonitorObject(MonitorType.MonitorCompleteChannel, channel.getName(), from,
              "completed [" + channel.getName() + "]");
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        channel.complete();
        this.destroy(channel, from);

    }

    @Override
    public Channel getChannelObject(String cname, String from) {
       return this.getChannelObject(cname, from, false);
    }

    @Override
    public Channel getChannelObject(String cname, String from, boolean noRefCount) {
        Channel channel;
        String symbol = " [+] ";

        if (this.internalChannelMap.containsKey(cname)) {
            channel = this.internalChannelMap.get(cname);
        } else {
            channel = new Channel(cname);
            this.internalChannelMap.put(cname, channel);
            symbol = " [+++] ";
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorNewChannel, cname, from, symbol);
        this.monitorStream.send(new MessageObject<>(MessageType.MessageTypeRequest, mo));
        if (!noRefCount) {
            channel.increment();
        }
        return channel;
    }

    @Override
    public Observable<Message> getChannel(String channel, String from) {
        return this.getChannel(channel, from, false);
    }

    @Override
    public Observable<Message> getChannel(String channel, String from, boolean noRefCount) {
        return this.getChannelObject(channel, from, noRefCount)
              .getStreamObject();
    }

    @Override
    public Observable<Message> getRequestChannel(String channel, String from) {
        return this.getRequestChannel(channel, from, false);
    }

    @Override
    public Observable<Message> getRequestChannel(String channel, String from, boolean noRefCount) {
        return this.getChannel(channel, from, noRefCount)
              .filter(
                    (Message message) -> message.isRequest()
              );
    }

    @Override
    public Observable<Message> getResponseChannel(String channel, String from) {
        return this.getResponseChannel(channel, from, false);
    }

    @Override
    public Observable<Message> getResponseChannel(String channel, String from, boolean noRefCount) {
        return this.getChannel(channel, from, noRefCount)
              .filter(
                    (Message message) -> message.isResponse()
              );
    }

    @Override
    public Observable<Message> getErrorChannel(String channel, String from) {
        return this.getErrorChannel(channel, from, false);
    }

    @Override
    public Observable<Message> getErrorChannel(String channel, String from, boolean noRefCount) {
        return this.getChannel(channel, from, noRefCount)
              .filter(
                    (Message message) -> message.isError()
              );
    }

    @Override
    public void send(String channel, MessageObject messageObject, String from) {
        MonitorObject mo;

        if (!this.internalChannelMap.containsKey(channel)) {
            mo = new MonitorObject(MonitorType.MonitorDropped, channel, from, messageObject);
            this.monitorStream.send(new MessageObject<>(MessageType.MessageTypeRequest, mo));
            return;
        }

        MonitorType type = MonitorType.MonitorData;
        switch (messageObject.getType()) {
            case MessageTypeError:
                type = MonitorType.MonitorError;
        }

        this.logTraceMessage("Sending payload to channel '" + channel + "'", messageObject.getPayload().toString());

        mo = new MonitorObject(type, channel, from, messageObject);
        this.monitorStream.send(new MessageObject<>(MessageType.MessageTypeRequest, mo));
        this.internalChannelMap.get(channel).send(messageObject);

    }

    @Override
    public void error(String channel, Error error) {
        if (!this.internalChannelMap.containsKey(channel)) {
            return;
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorError, channel, "bus error", error);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeError, mo));
        this.internalChannelMap.get(channel).error(error);
    }

    private void destroy(Channel channel, String from) {
        MonitorObject mo = new MonitorObject(MonitorType.MonitorDestroyChannel, channel.getName(), from);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        this.internalChannelMap.remove(channel.getName());
    }
}
