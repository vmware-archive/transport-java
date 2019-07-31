/**
 * Copyright(c) VMware Inc. 2017
 */
package com.vmware.bifrost.bus.model;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.UUID;

public interface Message<T> {

    void setTargetUser(String targetUser);

    String getTargetUser();

    void setPayload(T payload);

    T getPayload();

    String getPayloadAsString() throws JsonProcessingException;

    void setPayloadClass(Class<T> payloadClass);

    Class<T> getPayloadClass();

    boolean isRequest();

    boolean isResponse();

    boolean isError();

    UUID getId();

    void setId(UUID id);

    void setHeaders(MessageHeaders headers);

    MessageHeaders getHeaders();

    Object getHeader(String headerName);
}
