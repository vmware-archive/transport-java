/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.config;

import com.vmware.transport.bridge.spring.config.interceptors.TransportDestinationMatcher;
import com.vmware.transport.bridge.spring.config.interceptors.TransportStompInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;

import java.util.EnumSet;

/**
 * Contains information for an already registered {@link TransportStompInterceptor} instance.
 */
public class StompInterceptorRegistration {

    public final TransportStompInterceptor interceptor;
    public final EnumSet<StompCommand> commandSet;
    public final TransportDestinationMatcher destinationMatcher;
    public final int priority;

    public StompInterceptorRegistration(
          TransportStompInterceptor interceptor,
          EnumSet<StompCommand> commandSet,
          TransportDestinationMatcher destinationMatcher,
          int priority) {

        this.interceptor = interceptor;
        this.commandSet = commandSet;
        this.destinationMatcher = destinationMatcher;
        this.priority = priority;
    }
}
