package com.vmware.bifrost.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.vmware.bifrost.AbstractService;
import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostPeer;
import com.vmware.bifrost.bus.model.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.reactivex.Observable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static com.vmware.bifrost.bus.model.MonitorChannel.*;


/**
 * Copyright(c) VMware Inc. 2017
 */

@SuppressWarnings("unchecked")
@Component
public class MessagebusService extends AbstractService {

    @Autowired
    private ApplicationContext context;

    @EventListener
    public void handleContextStarted(ContextRefreshedEvent evt) {
        this.init();
    }

    private Map<String, Channel> channelMap;
    private Channel monitorStream;
    private String monitorChannel;
    private boolean dumpMonitor;
    private Logger logger;

    private JsonSchema schema;
    private ObjectMapper mapper;
    private JsonSchemaGenerator schemaGen;

    public MessagebusService() throws Exception {

        System.out.println("CREATING NEW BUS SERVICE");

        this.mapper = new ObjectMapper();
        this.schemaGen = new JsonSchemaGenerator(mapper);
        this.schema = schemaGen.generateSchema(MessageSchema.class);

        logger = LoggerFactory.getLogger(MessagebusService.class);
        this.channelMap = new HashMap<>();
        this.monitorChannel = stream;
        this.monitorStream = new Channel(this.monitorChannel);
        this.channelMap.put(this.monitorChannel, this.monitorStream);

        this.enableMonitorDump(true);
    }


    public boolean enableMonitorDump(boolean flag) {
        this.dumpMonitor = flag;
        return this.dumpMonitor;
    }

    public void init() {
        logger.info(":-) Starting Bifröst");
        Map<String, Object> peerBeans = context.getBeansWithAnnotation(BifrostPeer.class);
        for (Map.Entry<String, Object> entry : peerBeans.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof BifrostEnabled) {
                ((BifrostEnabled) value).initializeSubscriptions();
            }
        }
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
        String symbol = " [+] ";

        if (this.channelMap.containsKey(cname)) {
            channel = this.channelMap.get(cname);
        } else {
            channel = new Channel(cname);
            this.channelMap.put(cname, channel);
            symbol = " [+++] ";
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorNewChannel, cname, from, symbol);
        this.monitorStream.send(new MessageObject<>(MessageType.MessageTypeRequest, mo));
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

        logger.debug("[!] Bifröst Bus: channel closing '" + cname + "'");

        Channel channel = this.channelMap.get(cname);
        channel.decrement();
        MonitorObject mo = new MonitorObject(
                MonitorType.MonitorCloseChannel, cname, from,
                "close [" + cname.trim() + "] " + channel.getRefCount() + " references remaining");

        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));

        if (channel.getRefCount() == 0) {
            this.complete(channel, from);
        }
    }

    public void complete(Channel channel, String from) {

        MonitorObject mo = new MonitorObject(MonitorType.MonitorCompleteChannel, channel.getName(), from,
                "completed [" + channel.getName() + "]");
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        channel.complete();
        this.destroy(channel, from);

    }

    public void complete(String channel, String from) {
        Channel chan = this.getChannelObject(channel, from);
        this.complete(chan, from);
    }

    private void destroy(Channel channel, String from) {
        MonitorObject mo = new MonitorObject(MonitorType.MonitorDestroyChannel, channel.getName(), from);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeResponse, mo));
        this.channelMap.remove(channel.getName());
    }

    public void send(String channel, MessageObject messageObject, String from) {
        // TEMPORARY - flag all messages without schema
        if (messageObject.getSchema() == null) {
            //this.logger.warn("* No schema in messageObject to " + cname, from);
        }
        MonitorObject mo;

        if (!this.channelMap.containsKey(channel)) {
            mo = new MonitorObject(MonitorType.MonitorDropped, channel, from, messageObject);
            this.monitorStream.send(new MessageObject<>(MessageType.MessageTypeRequest, mo));
            return;
        }

        MonitorType type = MonitorType.MonitorData;
        switch (messageObject.getType()) {
            case MessageTypeError:
                type = MonitorType.MonitorError;
        }

        mo = new MonitorObject(type, channel, from, messageObject);
        this.monitorStream.send(new MessageObject<>(MessageType.MessageTypeRequest, mo));
        this.channelMap.get(channel).send(messageObject);

    }

    public void sendRequest(String channel, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        this.send(config.getSendChannel(), config, this.getName());

    }

    public void sendRequest(String channel, Object payload) {
        this.sendRequest(channel, payload, this.schema);
    }

    public void sendResponse(String channel, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        this.send(config.getSendChannel(), config, this.getName());
    }

    public void sendResponse(String channel, Object payload) {
        this.sendResponse(channel, payload, this.schema);
    }

    public void sendError(String channel, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeError, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        this.send(config.getSendChannel(), config, this.getName());
    }

    public void sendError(String channel, Object payload) {
        this.sendError(channel, payload, this.schema);
    }


    public void error(String channel, Error error) {
        if (!this.channelMap.containsKey(channel)) {
            return;
        }

        MonitorObject mo = new MonitorObject(MonitorType.MonitorError, channel, "bus error", error);
        this.monitorStream.send(new MessageObject(MessageType.MessageTypeError, mo, schema));
        this.channelMap.get(channel).error(error);
    }

    public Observable<Message> getRequestChannel(String channel, String from) {
        return this.getChannel(channel, from)
                .filter(
                        (Message message) -> message.isRequest()
                );
    }

    public Observable<Message> getResponseChannel(String channel, String from) {
        return this.getChannel(channel, from)
                .filter(
                        (Message message) -> message.isResponse()
                );
    }

    public Observable<Message> getErrorChannel(String channel, String from) {
        return this.getChannel(channel, from)
                .filter(
                        (Message message) -> message.isError()
                );
    }

    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      Consumer<Message> successHandler,
                                      Consumer<Message> errorHandler) {
        return this.requestOnce(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, errorHandler);
    }


    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      Consumer<Message> successHandler) {
        return this.requestOnce(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, null);
    }

    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      Consumer<Message> successHandler) {
        return this.requestOnce(sendChannel, payload,
                sendChannel, null, this.getName(), successHandler, null);
    }

    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      JsonSchema schema,
                                      String from,
                                      Consumer<Message> successHandler,
                                      Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload);

        config.setSingleResponse(true);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);
        config.setSchema(schema);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);
        this.send(config.getSendChannel(), config, from);

        BusTransaction transaction = new BusHandlerTransaction(sub, messageHandler);
        return transaction;
    }


    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        JsonSchema schema,
                                        String from,
                                        Consumer<Message> successHandler,
                                        Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload);

        config.setSingleResponse(false);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);
        config.setSchema(schema);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);
        this.send(config.getSendChannel(), config, from);

        BusTransaction transaction = new BusHandlerTransaction(sub, messageHandler);
        return transaction;
    }

    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        Consumer<Message> successHandler,
                                        Consumer<Message> errorHandler) {
        return this.requestStream(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, errorHandler);
    }

    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        Consumer<Message> successHandler) {
        return this.requestStream(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, null);
    }

    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        Consumer<Message> successHandler) {
        return this.requestStream(sendChannel, payload,
                sendChannel, null, this.getName(), successHandler, null);
    }


    public BusTransaction listenStream(String channel,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler) {

        return this.listenStream(channel, null, successHandler, errorHandler);
    }


    public BusTransaction listenStream(String channel,
                                       Consumer<Message> successHandler) {

        return this.listenStream(channel, null, successHandler, null);
    }

    public BusTransaction listenStream(String channel,
                                       JsonSchema schema,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, null);

        config.setSingleResponse(false);
        config.setReturnChannel(channel);
        config.setSendChannel(channel);
        config.setSchema(schema);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);

        return new BusHandlerTransaction(sub, messageHandler);
    }


    public BusTransaction respondOnce(String sendChannel,
                                      String returnChannel,
                                      JsonSchema schema,
                                      Function<Message, Object> generateHandler) {

        MessageObjectHandlerConfig config = new MessageObjectHandlerConfig();
        config.setSingleResponse(true);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);
        config.setSchema(schema);

        MessageResponder messageResponder = this.createMessageResponder(config, false);
        Disposable sub = messageResponder.generate(generateHandler);
        BusTransaction transaction = new BusResponderTransaction(sub, messageResponder);
        return transaction;
    }

    public BusTransaction respondStream(String sendChannel,
                                        String returnChannel,
                                        JsonSchema schema,
                                        Function<Message, Object> generateHandler) {

        MessageObjectHandlerConfig config = new MessageObjectHandlerConfig();
        config.setSingleResponse(false);
        config.setReturnChannel(returnChannel);
        config.setSendChannel(sendChannel);
        config.setSchema(schema);

        MessageResponder messageResponder = this.createMessageResponder(config, false);
        Disposable sub = messageResponder.generate(generateHandler);
        BusTransaction transaction = new BusResponderTransaction(sub, messageResponder);
        return transaction;
    }


    public BusTransaction respondOnce(String sendChannel,
                                      String returnChannel,
                                      Function<Message, Object> generateHandler) {
        return this.respondOnce(sendChannel, returnChannel, null, generateHandler);
    }

    public BusTransaction respondOnce(String sendChannel,
                                      Function<Message, Object> generateHandler) {
        return this.respondOnce(sendChannel, sendChannel, null, generateHandler);
    }

    public BusTransaction respondStream(String sendChannel,
                                        String returnChannel,
                                        Function<Message, Object> generateHandler) {
        return this.respondStream(sendChannel, returnChannel, null, generateHandler);
    }

    public BusTransaction respondStream(String sendChannel,
                                        Function<Message, Object> generateHandler) {
        return this.respondStream(sendChannel, sendChannel, null, generateHandler);
    }

    private MessageHandler createMessageHandler(MessageObjectHandlerConfig config, boolean requestStream) {
        return new MessageHandlerImpl(requestStream, config, this);
    }

    private MessageResponder createMessageResponder(MessageObjectHandlerConfig config, boolean requestStream) {
        return new MessageResponderImpl(requestStream, config, this);
    }
}
