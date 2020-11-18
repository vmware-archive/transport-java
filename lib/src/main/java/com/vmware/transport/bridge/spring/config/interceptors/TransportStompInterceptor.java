/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.config.interceptors;

import org.springframework.messaging.Message;

/**
 * Interface for interceptors that are able to view and/or modify the
 * {@link Message Messages} before they are sent to Transport channels.
 */
public interface TransportStompInterceptor {

    /**
     * Invoked before the Message is actually sent to the channel.
     * This allows for modification of the Message if necessary.
     * If this method returns {@code null} then the actual
     * send invocation will not occur.
     */
    Message<?> preSend(Message<?> message);
}
