/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.spring.config;

/**
 * Defines methods for configuring Transport STOMP bridge.
 */
public interface TransportBridgeConfigurer {

   default void registerTransportDestinationPrefixes(TransportBridgeConfiguration configuration) { };

   default void registerTransportStompInterceptors(TransportBridgeConfiguration configuration) { };

   default void configureGalacticChannels() {};
}
