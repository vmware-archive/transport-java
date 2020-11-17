/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.broker;

/**
 * Base class for galactic channel subscriptions.
 *
 * Each concrete MessageBrokerConnector implementation should define its own
 * MessageBrokerSubscription class containing the data specific to the MessageBroker.
 */
public abstract class MessageBrokerSubscription {
}
