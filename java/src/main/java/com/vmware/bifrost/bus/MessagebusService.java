package com.vmware.bifrost.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.vmware.bifrost.AbstractService;
import com.vmware.bifrost.bus.model.*;
import io.reactivex.subjects.Subject;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Observable;

import java.util.HashMap;
import java.util.Map;


/**
 * Copyright(c) VMware Inc. 2017
 */

public class MessagebusService extends AbstractService {

    private Map<String, Channel> channelMap;
    private Channel monitorStream;
    private String monitorChannel;
    private boolean dumpMonitor;
    private Logger logger;

    private JsonSchema schema;
    private ObjectMapper mapper;
    private JsonSchemaGenerator schemaGen;

    public MessagebusService() throws Exception {

        this.mapper = new ObjectMapper();
        this.schemaGen = new JsonSchemaGenerator(mapper);
        this.schema = schemaGen.generateSchema(MessageSchema.class);

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
        this.monitorStream.send(new MessageObject<MonitorObject>(MessageType.MessageTypeRequest, mo));
        channel.increment();
        return channel;
    }

    public Observable<Message> getChannel(String channelName, String from) {
        return this.getChannelObject(channelName, from)
                .getStreamObject();
    }

    public void close(String cname, String from) {
        if (!this.channelMap.containsKey(cname)) {
            return;
        }

        Channel channel = this.channelMap.get(cname);
        channel.decrement();
        MonitorObject mo = new MonitorObject(
                MonitorType.MonitorCloseChannel, cname, from,
                ' ' + channel.getRefCount());

        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));

        if (channel.getRefCount() == 0) {
            this.complete(channel, from);
        }
    }

    public void complete(Channel channel, String from) {

        MonitorObject mo = new MonitorObject(MonitorType.MonitorCompleteChannel, channel.getName(), from);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        channel.complete();
        this.destroy(channel, from);

    }

    public void complete(String cname, String from) {
        Channel channel = this.getChannelObject(cname, from);
        if(channel == null) {
            return;
        }
        this.complete(channel, from);
    }

    private void destroy(Channel channel, String from) {
        MonitorObject mo = new MonitorObject(MonitorType.MonitorDestroyChannel, channel.getName(), from);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        this.channelMap.remove(channel.getName());
    }

    public void send(String cname, MessageObject messageObject, String from) {
        // TEMPORARY - flag all messages without schema
        if (messageObject.getSchema() == null) {
            //this.logger.warn("* No schema in messageObject to " + cname, from);
        }
        MonitorObject mo;

        if (!this.channelMap.containsKey(cname)) {
            mo = new MonitorObject(MonitorType.MonitorDropped, cname, from, messageObject);
            this.monitorStream.send(new MessageObject<MonitorObject>(MessageType.MessageTypeRequest, mo));
            return;
        }

        mo = new MonitorObject(MonitorType.MonitorData, cname, from, messageObject);
        this.monitorStream.send(new MessageObject<MonitorObject>(MessageType.MessageTypeRequest, mo));
        this.channelMap.get(cname).send(messageObject);

    }

    public void sendRequest(String cname, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(cname);
        config.setReturnChannel(cname);
        this.send(config.getSendChannel(), config, this.getName());

    }

    public void sendRequest(String cname, Object payload) {
        this.sendRequest(cname, payload, this.schema);
    }

    public void sendResponse(String cname, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(cname);
        config.setReturnChannel(cname);
        this.send(config.getSendChannel(), config, this.getName());
    }

    public void sendResponse(String cname, Object payload) {
        this.sendResponse(cname, payload, this.schema);
    }

    public void sendError(String cname, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeError, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(cname);
        config.setReturnChannel(cname);
        this.send(config.getSendChannel(), config, this.getName());
    }

    public void sendError(String cname, Object payload) {
        this.sendError(cname, payload, this.schema);
    }


    public void error(String cname, Error error) {
        if (!this.channelMap.containsKey(cname)) {
            return;
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorError, cname, "bus error", error);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeError, mo, schema));
        this.channelMap.get(cname).error(error);
    }

    public Observable<Message> getRequestChannel(String cname, String from) {
        return this.getChannel(cname, from)
                .filter(
                        (Message message) -> {
                            return (message.isRequest() || message.isError());
                        }
                );
    }

    public Observable<Message> getResponseChannel(String cname, String from) {
        return this.getChannel(cname, from)
                .filter(
                        (Message message) -> {
                            return (message.isResponse() || message.isError());
                        }
                );
    }


//    private MessageHandler createMessageHandler(MessageObjectHandlerConfig handlerConfig, boolean requestStream, String name) {
//
//
//    }


}
