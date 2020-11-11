/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bus;

import com.vmware.transport.bus.model.Channel;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.model.MessageHeaders;
import com.vmware.transport.bus.model.MessageObject;
import com.vmware.transport.bus.model.MessageType;
import com.vmware.transport.bus.model.MonitorChannel;
import com.vmware.transport.bus.model.MonitorObject;
import com.vmware.transport.bus.model.MonitorType;
import com.vmware.transport.bus.model.SystemChannels;
import com.vmware.transport.core.util.Loggable;
import io.reactivex.Observable;
import io.reactivex.subjects.Subject;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class EventBusLowApiImpl extends Loggable implements EventBusLowApi {

    private final Map<String, Channel> internalChannelMap;

    private final Map<String, Map<String, Object>> channelAttributesMap;

    private Channel monitorStream;
    private Channel extMsgBrStream;
    private String monitorChannel;

    private boolean dumpMonitor;

    public EventBusLowApiImpl(Map<String, Channel> channelMap) {
        this.internalChannelMap = channelMap;

        this.channelAttributesMap = new ConcurrentHashMap<>();

        this.monitorChannel = MonitorChannel.stream;
        this.monitorStream = new Channel(this.monitorChannel);
        this.internalChannelMap.put(this.monitorChannel, this.monitorStream);

        this.extMsgBrStream = getChannelObject(
              SystemChannels.EXTERNAL_MESSAGE_BROKER, "EventBusLowApiImpl");
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
                this.channelAttributesMap.remove(cname);
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
                this.channelAttributesMap.put(cname, new ConcurrentHashMap<>());
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
    public int getChannelRefCount(String channel) {
        synchronized (this.internalChannelMap) {
            Channel channelObject = this.internalChannelMap.get(channel);
            if (channelObject != null) {
                return channelObject.getRefCount();
            }
        }
        return 0;
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
    public Object getChannelAttribute(String channel, String attribute) {
        Map<String, Object> attributes = channelAttributesMap.get(channel);
        if (attributes != null) {
            return attributes.get(attribute);
        }
        return null;
    }

    @Override
    public boolean setChannelAttribute(String channel, String attribute, Object attributeValue) {
        Map<String, Object> attributes = channelAttributesMap.get(channel);
        if (attributes != null) {
            attributes.put(attribute, attributeValue);
            return true;
        }
        return false;
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

            // If the channel is missing but the message has EXTERNAL_MESSAGE_BROKER_DESTINATION
            // header, instead of dropping the message, try sending it to
            // the external message broker system channel.
            if (messageObject.getHeader(
                    MessageHeaders.EXTERNAL_MESSAGE_BROKER_DESTINATION) != null) {
                this.logDebugMessage(String.format(
                      "Missing channel '%s', sending message to external topic: %s",
                      channel,
                      messageObject.getHeader(MessageHeaders.EXTERNAL_MESSAGE_BROKER_DESTINATION)));
                extMsgBrStream.send(messageObject);
                return;
            }

            this.logWarnMessage(String.format("Failed to send message. Cannot find channel: %s", channel));
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
                this.channelAttributesMap.remove(channel.getName());
            }
        }
    }
}
