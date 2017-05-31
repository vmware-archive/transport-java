package com.vmware.bifrost.bus.model;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageHandlerConfig<T> extends Message<T> {

    private String sendChannel;
    private String returnChannel;
    private boolean singleResponse;

    MessageHandlerConfig() {
        super();
    }

    public MessageHandlerConfig(MessageType type, Object payload) {
        super(type, payload);
    }

    public MessageHandlerConfig(MessageType type, Object payload, MessageSchema schema) {
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
