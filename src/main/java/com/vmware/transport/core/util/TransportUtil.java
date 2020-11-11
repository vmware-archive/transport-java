/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.core.util;

import com.vmware.transport.bridge.spring.config.TransportBridgeConfiguration;

public class TransportUtil {

    private TransportUtil() {}

    public static String getTransportDestinationPrefix(TransportBridgeConfiguration config, String destination) {
        if (destination == null) {
            return null;
        }
        destination = destination.toLowerCase().trim();
        for (String prefix : config.getTransportDestinationPrefixes()) {
            if (destination.startsWith(prefix.toLowerCase())) {
                return prefix;
            }
        }
        return null;
    }

    public static String extractChannelName(TransportBridgeConfiguration config, String destination) {
        String destinationPrefix = getTransportDestinationPrefix(config, destination);
        if (destination != null && destinationPrefix != null) {
            return destination.substring(destinationPrefix.length()).trim();
        }
        return null;
    }
}
