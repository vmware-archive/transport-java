/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config.interceptors;

/**
 * Interface used to determine whether a given STOMP destination matches
 * given criteria.
 */
public interface TransportDestinationMatcher {

    /**
     * Return true if the provided destination matches your criteria.
     */
    boolean match(String destination);
}
