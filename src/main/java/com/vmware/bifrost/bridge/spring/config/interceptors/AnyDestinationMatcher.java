/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config.interceptors;

/**
 * Simple {@link BifrostDestinationMatcher} matching any destination.
 */
public class AnyDestinationMatcher implements BifrostDestinationMatcher {

    @Override
    public boolean match(String destination) {
        return true;
    }
}
