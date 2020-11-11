/*
 * Copyright 2018 VMware, Inc. All rights reserved.
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
