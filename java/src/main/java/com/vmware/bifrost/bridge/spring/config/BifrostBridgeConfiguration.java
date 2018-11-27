/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains Bifrost STOMP bridge configuration.
 */
public class BifrostBridgeConfiguration {

   private final Set<String> bifrostDestinationPrefixes = new HashSet<>();

   /**
    * Returns all registered Bifrost destination prefixes.
    */
   public Set<String> getBifrostDestinationPrefixes() {
      return Collections.unmodifiableSet(bifrostDestinationPrefixes);
   }

   /**
    * Configures one or more prefixes used to filter Bifrost destinations
    * (e.g. destinations prefixed with "/topic").
    *
    * Destination prefixes that do not have a trailing slash
    * will have one automatically appended.
    */
   public void addBifrostDestinationPrefixes(String... destinationPrefixes) {
      for (String prefix : destinationPrefixes) {
         prefix = prefix.trim();
         if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
         }
         bifrostDestinationPrefixes.add(prefix);
      }
   }
}
