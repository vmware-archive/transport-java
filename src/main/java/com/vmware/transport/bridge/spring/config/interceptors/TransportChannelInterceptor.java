/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config.interceptors;

import com.vmware.transport.bridge.spring.config.TransportBridgeConfiguration;
import com.vmware.transport.bridge.spring.config.StompInterceptorRegistration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;

import java.util.List;

/**
 * {@link ChannelInterceptorAdapter} instance responsible to apply all registered
 * {@link TransportStompInterceptor} instances in the correct order.
 */
public class TransportChannelInterceptor extends ChannelInterceptorAdapter {

    private TransportBridgeConfiguration configuration;

    public TransportChannelInterceptor(TransportBridgeConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        List<StompInterceptorRegistration> interceptors =
              this.configuration.getRegisteredTransportStompInterceptors();

        if (interceptors.isEmpty()) {
            // Do nothing if there are no registered custom interceptors.
            return message;
        }

        StompHeaderAccessor header = StompHeaderAccessor.wrap(message);
        String destination = header.getDestination();

        if (destination == null || destination.isEmpty()) {
            // Ignore messages without valid destination.
            return message;
        }

        StompCommand stompCommand = header.getCommand();

        // this.configuration.interceptors list should be sorted by priority.
        for (StompInterceptorRegistration interceptorRegistration : interceptors) {

            // Determine whether the current interceptor is applicable for the incoming
            // message.
            if (interceptorRegistration.commandSet.contains(stompCommand) &&
                  interceptorRegistration.destinationMatcher.match(destination)) {
                // apply the interceptor and update the message
                message = interceptorRegistration.interceptor.preSend(message);
            }
            if (message == null) {
                break;
            }
        }
        return message;
    }
}
