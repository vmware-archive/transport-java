/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
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
