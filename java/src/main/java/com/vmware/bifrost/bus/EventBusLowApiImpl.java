/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus;

import com.vmware.bifrost.bus.model.Channel;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObject;
import com.vmware.bifrost.bus.model.MessageType;
import com.vmware.bifrost.bus.model.MonitorChannel;
import com.vmware.bifrost.bus.model.MonitorObject;
import com.vmware.bifrost.bus.model.MonitorType;
import com.vmware.bifrost.core.util.Loggable;
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
    public Channel getMonitorStream() {
        return this.monitorStream;
    }

    @Override
    public void close(String cname, String from) {

        Channel channel;
        synchronized (this.internalChannelMap) {
            channel = this.internalChannelMap.get(cname);
            if (channel == null) {
                return;
            }
            if (channel.decrement() == 0) {
                this.internalChannelMap.remove(cname);
            }
        }

        MonitorObject mo = new MonitorObject(
              MonitorType.MonitorCloseChannel, cname, from,
              "close [" + cname.trim() + "] " + channel.getRefCount() + " references remaining");

        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));

        if (channel.getRefCount() == 0) {
            // Complete and destroy the channel without removing it from the internalChannelMap,
            // as it's already removed.
            this.completeAndDestroyInternal(channel, from, false);
        }
    }

    @Override
    public void complete(String channel, String from) {
        Channel chan = this.getChannelObject(channel, from, true);
        this.complete(chan, from);
    }

    @Override
    public void complete(Channel channel, String from) {
        completeAndDestroyInternal(channel, from, true);
    }

    private void completeAndDestroyInternal(Channel channel, String from, boolean removeFromChannelMap) {
        MonitorObject mo = new MonitorObject(MonitorType.MonitorCompleteChannel, channel.getName(), from,
              "completed [" + channel.getName() + "]");
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        channel.complete();
        this.destroy(channel, from, removeFromChannelMap);
    }

    @Override
    public Channel getChannelObject(String cname, String from) {
       return this.getChannelObject(cname, from, false);
    }

    @Override
    public Channel getChannelObject(String cname, String from, boolean noRefCount) {
        Channel channel;
        String symbol = " [+] ";

        synchronized (this.internalChannelMap) {
            if (this.internalChannelMap.containsKey(cname)) {
                channel = this.internalChannelMap.get(cname);
            } else {
                channel = new Channel(cname);
                this.internalChannelMap.put(cname, channel);
                symbol = " [+++] ";
            }
            if (!noRefCount) {
                channel.increment();
            }
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorNewChannel, cname, from, symbol);
        this.monitorStream.send(new MessageObject<>(MessageType.MessageTypeRequest, mo));

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

        Channel channelObj;
        synchronized (this.internalChannelMap) {
            channelObj = this.internalChannelMap.get(channel);
        }

        if (channelObj == null) {
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
        channelObj.send(messageObject);

    }

    @Override
    public void error(String channel, Error error) {
        Channel channelObj;
        synchronized (this.internalChannelMap) {
            channelObj = this.internalChannelMap.get(channel);
        }

        if (channelObj == null) {
            return;
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorError, channel, "bus error", error);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeError, mo));
        channelObj.error(error);
    }

    private void destroy(Channel channel, String from, boolean removeFromMap) {
        MonitorObject mo = new MonitorObject(MonitorType.MonitorDestroyChannel, channel.getName(), from);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        if (removeFromMap) {
            synchronized (this.internalChannelMap) {
                this.internalChannelMap.remove(channel.getName());
            }
        }
    }
}
