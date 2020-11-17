/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config.interceptors;

/**
 * Simple {@link TransportDestinationMatcher} matching any destination.
 */
public class AnyDestinationMatcher implements TransportDestinationMatcher {

    @Override
    public boolean match(String destination) {
        return true;
    }
}
