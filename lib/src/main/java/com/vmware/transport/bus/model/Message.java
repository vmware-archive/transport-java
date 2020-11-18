/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

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
