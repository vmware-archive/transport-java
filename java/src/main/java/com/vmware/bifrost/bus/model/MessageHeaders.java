/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Helper class used to store headers for Bifrost messages.
 */
public class MessageHeaders {

    public static final String EXTERNAL_MESSAGE_BROKER_DESTINATION =
          "ext-msg-broker-destination";

    private final Map<String, Object> headers;

    MessageHeaders() {
        headers = new HashMap<>();
    }

    /**
     * Add new or change the value of existing message header.
     *
     * @param headerName,  the name of the message header.
     * @param headerValue, the value of the message header.
     * @return reference to this object.
     */
    public MessageHeaders setHeader(String headerName, Object headerValue) {
        headers.put(headerName, headerValue);
        return this;
    }

    /**
     * Return the value of a message header.
     *
     * @param headerName, the name of the header.
     * @return the value of header or null if it doesn't exist.
     */
    public Object getHeader(String headerName) {
        return headers.get(headerName);
    }

    /**
     * Return a {@link Set<String>} containing all header names.
     */
    public Set<String> getHeaderNames() {
        return Collections.unmodifiableSet(headers.keySet());
    }

    /**
     * Create new MessageHeader instance.
     */
    public static MessageHeaders newInstance() {
        return new MessageHeaders();
    }

    /**
     * Create new MessageHeader instance and add a single header to it.
     * Same as calling MessageHeaders.newInstance().setHeader(...)
     */
    public static MessageHeaders newInstance(String headerName, Object headerValue) {
        return newInstance().setHeader(headerName, headerValue);
    }
}
