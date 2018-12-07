/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.bus.model.Channel;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import com.vmware.bifrost.bus.model.MessageSchema;
import com.vmware.bifrost.bus.model.MessageType;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import static com.vmware.bifrost.bus.model.MonitorChannel.stream;

@Component
@SuppressWarnings("unchecked")
public class EventBusImpl extends Loggable implements EventBus {

    private EventBusLowApi api;

    @Autowired
    private ApplicationContext context;

    @EventListener
    public void handleContextStarted(ContextRefreshedEvent evt) {
        this.init();
    }

    private Map<String, Channel> channelMap;
    private Channel monitorStream;
    private String monitorChannel;

    private JsonSchema schema;

    public EventBusImpl() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        this.schema = schemaGen.generateSchema(MessageSchema.class);

        this.channelMap = new HashMap<>();
        this.monitorChannel = stream;
        this.monitorStream = new Channel(this.monitorChannel);
        this.channelMap.put(this.monitorChannel, this.monitorStream);

        this.api = new EventBusLowApiImpl(this.channelMap, this.schema);
        this.api.enableMonitorDump(true);
    }

    @Override
    public EventBusLowApi getApi() {
        return api;
    }

    @Override
    public void sendRequestMessage(String channel, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        this.api.send(config.getSendChannel(), config, this.getName());

    }

    @Override
    public void sendRequestMessage(String channel, Object payload) {
        this.sendRequestMessage(channel, payload, this.schema);
    }

    @Override
    public void sendRequestMessageWithId(String channel, Object payload, UUID id) {
        this.sendRequestMessageWithId(channel, payload, id, this.schema);
    }

    @Override
    public void sendRequestMessageWithId(String channel, Object payload, UUID id, JsonSchema schema) {

        MessageObjectHandlerConfig config =
              new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setId(id);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendResponseMessage(String channel, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendResponseMessage(String channel, Object payload) {
        this.sendResponseMessage(channel, payload, this.schema);
    }

    @Override
    public void sendResponseMessageWithId(String channel, Object payload,  UUID id) {
        this.sendResponseMessageWithId(channel, payload, id, this.schema);
    }

    @Override
    public void sendResponseMessageWithId(String channel, Object payload,  UUID id, JsonSchema schema) {

        MessageObjectHandlerConfig config =
              new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setId(id);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendErrorMessage(String channel, Object payload, JsonSchema schema) {

        MessageObjectHandlerConfig config =
                new MessageObjectHandlerConfig(MessageType.MessageTypeError, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public void sendErrorMessage(String channel, Object payload) {
        this.sendErrorMessage(channel, payload, this.schema);
    }

    @Override
    public void sendErrorMessageWithId(String channel, Object payload, UUID id) {
        this.sendErrorMessageWithId(channel, payload, id, this.schema);
    }

    @Override
    public void sendErrorMessageWithId(String channel, Object payload, UUID id, JsonSchema schema) {

        MessageObjectHandlerConfig config =
              new MessageObjectHandlerConfig(MessageType.MessageTypeError, payload, schema);
        config.setSingleResponse(true);
        config.setSendChannel(channel);
        config.setReturnChannel(channel);
        config.setId(id);
        this.api.send(config.getSendChannel(), config, this.getName());
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      Consumer<Message> successHandler,
                                      Consumer<Message> errorHandler) {
        return this.requestOnce(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      Consumer<Message> successHandler) {
        return this.requestOnce(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      Consumer<Message> successHandler) {
        return this.requestOnce(sendChannel, payload,
                sendChannel, null, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestOnce(String sendChannel,
                                      Object payload,
                                      String returnChannel,
                                      JsonSchema schema,
                                      String from,
                                      Consumer<Message> successHandler,
                                      Consumer<Message> errorHandler) {

        return this.requestOnceInternal(null, sendChannel,
              payload, returnChannel, schema, from, successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     Consumer<Message> successHandler) {

        return this.requestOnceWithId(uuid, sendChannel, payload, sendChannel, successHandler);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     String returnChannel,
                                     Consumer<Message> successHandler) {

        return this.requestOnceWithId(uuid, sendChannel, payload, returnChannel, successHandler, null);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     String returnChannel,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler) {

        return this.requestOnceWithId(uuid, sendChannel, payload,
              returnChannel, null, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestOnceWithId(UUID uuid,
                                     String sendChannel,
                                     Object payload,
                                     String returnChannel,
                                     JsonSchema schema,
                                     String from,
                                     Consumer<Message> successHandler,
                                     Consumer<Message> errorHandler) {

        return this.requestOnceInternal(uuid, sendChannel, payload,
              returnChannel, schema, from, successHandler, errorHandler);
    }

    private BusTransaction requestOnceInternal(UUID id,
                                               String sendChannel,
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
        config.setId(id);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);
        this.api.send(config.getSendChannel(), config, from);

        BusTransaction transaction = new BusHandlerTransaction(sub, messageHandler);
        return transaction;
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        JsonSchema schema,
                                        String from,
                                        Consumer<Message> successHandler,
                                        Consumer<Message> errorHandler) {

        return this.requestStreamInternal(null, sendChannel, payload,
              returnChannel, schema, from, successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        Consumer<Message> successHandler,
                                        Consumer<Message> errorHandler) {
        return this.requestStream(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        String returnChannel,
                                        Consumer<Message> successHandler) {
        return this.requestStream(sendChannel, payload,
                returnChannel, null, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestStream(String sendChannel,
                                        Object payload,
                                        Consumer<Message> successHandler) {
        return this.requestStream(sendChannel, payload,
                sendChannel, null, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              Consumer<Message> successHandler) {

        return this.requestStreamWithId(uuid, sendChannel, payload,
              sendChannel, null, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              String returnChannel,
                                              Consumer<Message> successHandler) {

        return this.requestStreamWithId(uuid, sendChannel, payload,
              returnChannel, null, this.getName(), successHandler, null);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              String returnChannel,
                                              Consumer<Message> successHandler,
                                              Consumer<Message> errorHandler) {

        return this.requestStreamWithId(uuid, sendChannel, payload,
              returnChannel, null, this.getName(), successHandler, errorHandler);
    }

    @Override
    public BusTransaction requestStreamWithId(UUID uuid,
                                              String sendChannel,
                                              Object payload,
                                              String returnChannel,
                                              JsonSchema schema,
                                              String from,
                                              Consumer<Message> successHandler,
                                              Consumer<Message> errorHandler) {

        return this.requestStreamInternal(uuid, sendChannel, payload,
              returnChannel, schema, from, successHandler, errorHandler);
    }


    private BusTransaction requestStreamInternal(UUID uuid,
                                              String sendChannel,
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
        config.setId(uuid);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);
        this.api.send(config.getSendChannel(), config, from);

        BusTransaction transaction = new BusHandlerTransaction(sub, messageHandler);
        return transaction;
    }

    @Override
    public BusTransaction listenRequestStream(String channel,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler) {

        return this.listenRequestStream(channel, null, successHandler, errorHandler);
    }

    @Override
    public BusTransaction listenRequestStream(String channel,
                                       Consumer<Message> successHandler) {
        return this.listenRequestStream(channel, null, successHandler, null);
    }

    @Override
    public BusTransaction listenRequestStream(String channel,
                                       JsonSchema schema,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeRequest, null);

        config.setSingleResponse(false);
        config.setReturnChannel(channel);
        config.setSendChannel(channel);
        config.setSchema(schema);

        MessageHandler messageHandler = this.createMessageHandler(config, true);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);

        return new BusHandlerTransaction(sub, messageHandler);
    }

    @Override
    public BusTransaction listenStream(String channel,
                                       Consumer<Message> successHandler) {
        return this.listenStream(channel, null, successHandler, null);
    }

    @Override
    public BusTransaction listenStream(String channel,
                                       JsonSchema schema,
                                       Consumer<Message> successHandler,
                                       Consumer<Message> errorHandler) {

        MessageObjectHandlerConfig config
                = new MessageObjectHandlerConfig(MessageType.MessageTypeResponse, null);

        config.setSingleResponse(false);
        config.setReturnChannel(channel);
        config.setSendChannel(channel);
        config.setSchema(schema);

        MessageHandler messageHandler = this.createMessageHandler(config, false);
        Disposable sub = messageHandler.handle(successHandler, errorHandler);

        return new BusHandlerTransaction(sub, messageHandler);
    }

    @Override
    public BusTransaction respondOnce(String sendChannel,
                                      String returnChannel,
                                      Function<Message, Object> generateHandler) {
        return this.respondOnce(sendChannel, returnChannel, null, generateHandler);
    }

    @Override
    public BusTransaction respondOnce(String sendChannel,
                                      Function<Message, Object> generateHandler) {
        return this.respondOnce(sendChannel, sendChannel, null, generateHandler);
    }

    @Override
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

    @Override
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

    @Override
    public BusTransaction respondStream(String sendChannel,
                                        String returnChannel,
                                        Function<Message, Object> generateHandler) {
        return this.respondStream(sendChannel, returnChannel, null, generateHandler);
    }

    @Override
    public BusTransaction respondStream(String sendChannel,
                                        Function<Message, Object> generateHandler) {
        return this.respondStream(sendChannel, sendChannel, null, generateHandler);
    }

    @Override
    public void closeChannel(String channel, String from) {
        this.api.close(channel, from);
    }

    private  void init() {
        this.logBannerMessage("\uD83C\uDF08","Starting Bifröst");
        Map<String, Object> peerBeans = context.getBeansWithAnnotation(BifrostService.class);
        for (Map.Entry<String, Object> entry : peerBeans.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof BifrostEnabled) {
                ((BifrostEnabled) value).initializeSubscriptions();
            }
        }
    }

    private MessageHandler createMessageHandler(MessageObjectHandlerConfig config, boolean requestStream) {
        return new MessageHandlerImpl(requestStream, config, this);
    }

    private MessageResponder createMessageResponder(MessageObjectHandlerConfig config, boolean requestStream) {
        return new MessageResponderImpl(requestStream, config, this);
    }
}
