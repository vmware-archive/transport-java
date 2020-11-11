/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.bridge.spring.services;

import java.util.Collection;

/**
 * Registry providing information for active bridge subscriptions.
 */
public interface TransportBridgeSubscriptionRegistry {

    /**
     * Returns all active subscriptions.
     */
    Collection<TransportSubscriptionService.TransportSubscription> getSubscriptions();

    /**
     * Returns all Transport channels with active bridge subscriptions.
     */
    Collection<String> getOpenChannels();

    /**
     * Returns all Transport channels with active bridge subscriptions and with a given
     * attribute value.
     */
    Collection<String> getOpenChannelsWithAttribute(String attribute, Object attributeValue);
}
