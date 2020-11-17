/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config;

import com.vmware.transport.bridge.spring.config.interceptors.TransportDestinationMatcher;
import com.vmware.transport.bridge.spring.config.interceptors.TransportStompInterceptor;
import org.springframework.messaging.simp.stomp.StompCommand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Contains Transport STOMP bridge configuration.
 */
public class TransportBridgeConfiguration {

   private final Set<String> transportDestinationPrefixes = new HashSet<>();

   /**
    * Returns all registered Transport destination prefixes.
    */
   public Set<String> getTransportDestinationPrefixes() {
      return Collections.unmodifiableSet(transportDestinationPrefixes);
   }

   /**
    * Configures one or more prefixes used to filter Transport destinations
    * (e.g. destinations prefixed with "/topic").
    *
    * Destination prefixes that do not have a trailing slash
    * will have one automatically appended.
    */
   public void addTransportDestinationPrefixes(String... destinationPrefixes) {
      for (String prefix : destinationPrefixes) {
         prefix = prefix.trim();
         if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
         }
         transportDestinationPrefixes.add(prefix);
      }
   }

   private final List<StompInterceptorRegistration> interceptors = new ArrayList<>();

   /**
    * Returns all {@link TransportStompInterceptor} registrations.
    */
   public List<StompInterceptorRegistration> getRegisteredTransportStompInterceptors() {
      return interceptors;
   }

   /**
    * Registers new {@link TransportStompInterceptor}.
    *
    * @param interceptor the {@link TransportStompInterceptor} to be registered.
    * @param commandSet a set of STOMP commands for which the interceptor should be applied.
    * @param destinationMatcher a {@link TransportDestinationMatcher} controlling
    *                           for which channels the interceptor should be applied.
    * @param priority the priority of the interceptor. Interceptors will be invoked in
    *                 ascending priority order.
    */
   public void addTransportStompInterceptor(
            TransportStompInterceptor interceptor,
            EnumSet<StompCommand> commandSet,
            TransportDestinationMatcher destinationMatcher,
            int priority) {

      interceptors.add(new StompInterceptorRegistration(
            interceptor, commandSet, destinationMatcher, priority));

      // Keep interceptors sorted by priority.
      interceptors.sort((o1, o2) -> o1.priority - o2.priority);
   }
}
