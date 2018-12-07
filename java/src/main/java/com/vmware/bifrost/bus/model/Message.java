/**
 * Copyright(c) VMware Inc. 2017
 */
package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.module.jsonSchema.JsonSchema;

import java.util.UUID;

public interface Message<T> {

    void setPayload(T payload);

    T getPayload();

    void setPayloadClass(Class<T> payloadClass);

    Class<T> getPayloadClass();

    JsonSchema getSchema();

    void setSchema(JsonSchema schema);

    boolean isRequest();

    boolean isResponse();

    boolean isError();

    UUID getId();

    void setId(UUID id);
}
