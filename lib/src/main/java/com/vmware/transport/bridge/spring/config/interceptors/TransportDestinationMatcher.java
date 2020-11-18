/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
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
