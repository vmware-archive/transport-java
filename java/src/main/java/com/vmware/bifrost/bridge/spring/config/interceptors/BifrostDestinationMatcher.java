/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config.interceptors;

/**
 * Interface used to determine whether a given STOMP destination matches
 * given criteria.
 */
public interface BifrostDestinationMatcher {

    /**
     * Return true if the provided destination matches your criteria.
     */
    boolean match(String destination);
}
