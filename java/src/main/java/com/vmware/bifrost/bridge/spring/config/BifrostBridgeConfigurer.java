/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

/**
 * Defines methods for configuring Bifrost STOMP bridge.
 */
public interface BifrostBridgeConfigurer {

   default void registerBifrostDestinationPrefixes(BifrostBridgeConfiguration configuration) { };

   default void registerBifrostStompInterceptors(BifrostBridgeConfiguration configuration) { };

   default void configureGalacticChannels() {};
}
