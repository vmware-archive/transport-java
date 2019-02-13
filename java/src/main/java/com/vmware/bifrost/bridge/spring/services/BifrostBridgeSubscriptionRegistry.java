/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.services;

import java.util.Collection;

/**
 * Registry providing information for active bridge subscriptions.
 */
public interface BifrostBridgeSubscriptionRegistry {

    /**
     * Returns all active subscriptions.
     */
    Collection<BifrostSubscriptionService.BifrostSubscription> getSubscriptions();

    /**
     * Returns all Bifrost channels with active bridge subscriptions.
     */
    Collection<String> getOpenChannels();
}
