/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config.interceptors;

/**
 * {@link BifrostDestinationMatcher} implementation which can be used to
 * match destinations starting with one of a predefined list of prefixes.
 */
public class StartsWithDestinationMatcher implements BifrostDestinationMatcher {

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
