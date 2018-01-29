package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

/**
 * Copyright(c) VMware Inc. 2017
 */
public interface Message<T> {

    public void setPayload(T payload);

    public T getPayload();

    public void setPayloadClass(Class<T> payloadClass);

    public Class<T> getPayloadClass();

    public JsonSchema getSchema();

    public void setSchema(JsonSchema schema);

    public boolean isRequest();

    public boolean isResponse();

    public boolean isError();
}
