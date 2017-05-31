package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.function.Supplier;

/**
 * Copyright(c) VMware Inc. 2017
 */
public class MessageSchema<T> {

    protected Class type;
    protected String title;
    protected String description;
    protected Supplier<T> payload;
    protected boolean isError;


    public MessageSchema(Supplier<T> payload) {
        type = MessageSchema.class;
        this.payload = payload;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty(required = true)
    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty(required = true)
    public  Supplier<T> getPayload() {
        return payload;
    }

    public void setPayload(Supplier<T> payload) {
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
