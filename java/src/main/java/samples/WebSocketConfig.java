/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples;

import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfiguration;
import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfigurer;
import com.vmware.bifrost.bridge.spring.config.interceptors.AnyDestinationMatcher;
import com.vmware.bifrost.bridge.spring.config.interceptors.StartsWithDestinationMatcher;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

import java.util.EnumSet;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer
      implements BifrostBridgeConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/pub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/bifrost").setAllowedOrigins("*");
    }

    @Override
    public void registerBifrostDestinationPrefixes(BifrostBridgeConfiguration configuration) {
        configuration.addBifrostDestinationPrefixes("/topic", "/pub");
    }

    @Override
    public void registerBifrostStompInterceptors(BifrostBridgeConfiguration configuration) {

        MessageLoggerInterceptor logger1 =
              new MessageLoggerInterceptor("[Logger1] SUBSCRIBE message to channel: ");
        MessageLoggerInterceptor logger2 =
              new MessageLoggerInterceptor("[Logger2] SUBSCRIBE message to channel: ");

        // Adds a new interceptor that logs every SUBSCRIBE message sent to
        // any channels.
        configuration.addBifrostStompInterceptor(
              logger1,
              EnumSet.of(StompCommand.SUBSCRIBE),
              new AnyDestinationMatcher(),
              1000);

        // Adds a new interceptor that logs every SUBSCRIBE message sent to
        // "/topic/servbot/" and "/topic/sample-stream/" channels.
        // Note that due to the higher priority logger2 will always be invoked before logger1.
        configuration.addBifrostStompInterceptor(
              logger2,
              EnumSet.of(StompCommand.SUBSCRIBE),
              new StartsWithDestinationMatcher("/topic/sample-stream/", "/topic/servbot/"),
              500);

        // Adds a new interceptor that drops every 5th request to servbot channel.
        configuration.addBifrostStompInterceptor(
              new DropStompMessageInterceptor(5),
              EnumSet.of(StompCommand.SEND),
              new StartsWithDestinationMatcher("/pub/servbot"),
              1000);
    }
}
