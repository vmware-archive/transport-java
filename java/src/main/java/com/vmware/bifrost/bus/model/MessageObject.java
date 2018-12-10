/**
 * Copyright(c) VMware Inc. 2017
 */
package com.vmware.bifrost.bus.model;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class MessageObject<T> implements Message<T> {

    @Setter
    @Getter
    protected MessageType type;

    @Setter
    @Getter
    protected Class<T> payloadClass;

    @Setter
    @Getter
    protected T payload;

    @Getter
    @Setter
    protected UUID id;

    public MessageObject() {
    }

    public MessageObject(MessageType type, T payload) {
        this.type = type;
        this.payload = payload;
        if(payload != null)
            this.payloadClass = (Class<T>) payload.getClass();

    }

    public boolean isRequest() {
        return this.type == MessageType.MessageTypeRequest;
    }

    public boolean isResponse() {
        return this.type == MessageType.MessageTypeResponse;
    }

    public boolean isError() {
        return this.type == MessageType.MessageTypeError;
    }

    public String toString() {
        return this.payload.toString();
    }

}
