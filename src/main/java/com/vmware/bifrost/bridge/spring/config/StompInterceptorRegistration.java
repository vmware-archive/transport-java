/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostDestinationMatcher;
import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostStompInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;

import java.util.EnumSet;

/**
 * Contains information for an already registered {@BifrostStompInterceptor} instance.
 */
public class StompInterceptorRegistration {

    public final BifrostStompInterceptor interceptor;
    public final EnumSet<StompCommand> commandSet;
    public final BifrostDestinationMatcher destinationMatcher;
    public final int priority;

    public StompInterceptorRegistration(
          BifrostStompInterceptor interceptor,
          EnumSet<StompCommand> commandSet,
          BifrostDestinationMatcher destinationMatcher,
          int priority) {

        this.interceptor = interceptor;
        this.commandSet = commandSet;
        this.destinationMatcher = destinationMatcher;
        this.priority = priority;
    }
}
