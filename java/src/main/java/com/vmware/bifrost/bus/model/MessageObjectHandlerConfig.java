package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageObjectHandlerConfig<T> extends MessageObject<T> {

    private String sendChannel;
    private String returnChannel;
    private boolean singleResponse;

    public MessageObjectHandlerConfig() {
        super();
    }

    public MessageObjectHandlerConfig(MessageType type, T payload) {
        super(type, payload);
    }

    public MessageObjectHandlerConfig(MessageType type, T payload, JsonSchema schema) {
        super(type, payload, schema);
    }

    public String getSendChannel() {
        return sendChannel;
    }

    public void setSendChannel(String sendChannel) {
        this.sendChannel = sendChannel;
    }

    public String getReturnChannel() {
        return returnChannel;
    }

    public void setReturnChannel(String returnChannel) {
        this.returnChannel = returnChannel;
    }

    public boolean isSingleResponse() {
        return singleResponse;
    }

    public void setSingleResponse(boolean singleResponse) {
        this.singleResponse = singleResponse;
    }
}
