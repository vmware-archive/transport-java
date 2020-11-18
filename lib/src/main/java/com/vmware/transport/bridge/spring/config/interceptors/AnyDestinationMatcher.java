/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
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
