/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples;

import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfiguration;
import com.vmware.bifrost.bridge.spring.config.BifrostBridgeConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

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
}
