/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostDestinationMatcher;
import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostStompInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
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

   private final List<StompInterceptorRegistration> interceptors = new ArrayList<>();

   /**
    * Returns all {@link BifrostStompInterceptor} registrations.
    */
   public List<StompInterceptorRegistration> getRegisteredBifrostStompInterceptors() {
      return interceptors;
   }

   /**
    * Registers new {@link BifrostStompInterceptor}.
    *
    * @param interceptor the {@link BifrostStompInterceptor} to be registered.
    * @param commandSet a set of STOMP commands for which the interceptor should be applied.
    * @param destinationMatcher a {@link BifrostDestinationMatcher} controlling
    *                           for which channels the interceptor should be applied.
    * @param priority the priority of the interceptor. Interceptors will be invoked in
    *                 ascending priority order.
    */
   public void addBifrostStompInterceptor(
            BifrostStompInterceptor interceptor,
            EnumSet<StompCommand> commandSet,
            BifrostDestinationMatcher destinationMatcher,
            int priority) {

      interceptors.add(new StompInterceptorRegistration(
            interceptor, commandSet, destinationMatcher, priority));

      // Keep interceptors sorted by priority.
      interceptors.sort((o1, o2) -> o1.priority - o2.priority);
   }
}
