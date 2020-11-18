/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.config.interceptors;

/**
 * {@link TransportDestinationMatcher} implementation which can be used to
 * match destinations starting with one of a predefined list of prefixes.
 */
public class StartsWithDestinationMatcher implements TransportDestinationMatcher {

    private String[] destinationPrefixes;

    public StartsWithDestinationMatcher(String... destinationPrefixes) {
        this.destinationPrefixes = destinationPrefixes;
    }

    @Override
    public boolean match(String destination) {
        for (String prefix : destinationPrefixes) {
            if (destination.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
