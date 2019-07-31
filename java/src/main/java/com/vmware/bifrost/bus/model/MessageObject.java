/**
 * Copyright(c) VMware Inc. 2017
 */
package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class MessageObject<T> implements Message<T> {

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
    }

    @Setter
    @Getter
    private String targetUser;

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

    @Getter
    @Setter
    protected MessageHeaders headers;

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

    public Object getHeader(String headerName) {
        if (headers != null) {
            return headers.getHeader(headerName);
        }
        return null;
    }

    public String getPayloadAsString() throws JsonProcessingException {
        if (payload instanceof String) {
            return (String) payload;
        }
        return MessageObject.objectMapper.writeValueAsString(payload);
    }

    public String toString() {
        return this.payload.toString();
    }

}
