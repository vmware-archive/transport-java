package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageSchema<T> {

    protected Class<T> payloadType;
    protected String title;
    protected String description;
    protected T payload;
    protected boolean isError;


    public MessageSchema(T payload) {
        this.payloadType =  (Class<T>) payload.getClass();
        this.payload = payload;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty(required = true)
    public Class<T> getPayloadType() {
        return payloadType;
    }

    public void setPayloadType(Class payloadType) {
        this.payloadType = payloadType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty(required = true)
    public  T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @JsonProperty(required = true)
    public boolean isError() {
        return isError;
    }

    public void setError(boolean error) {
        isError = error;
    }



}
