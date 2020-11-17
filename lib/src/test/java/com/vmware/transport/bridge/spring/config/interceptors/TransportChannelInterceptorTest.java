/*
 * Copyright 2018 VMware, Inc. All rights reserved.
 */
package com.vmware.transport.bridge.spring.config.interceptors;

import com.vmware.transport.bridge.spring.config.TransportBridgeConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.support.MessageBuilder;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class TransportChannelInterceptorTest {

    TransportChannelInterceptor channelInterceptor;
    TransportBridgeConfiguration configuration;

    @Before
    public void before() {
        this.configuration = new TransportBridgeConfiguration();
        this.channelInterceptor = new TransportChannelInterceptor(this.configuration);
    }

    @Test
    public void testPreSendWithEmptyInterceptors() {
        Message<String> message = generateMessage(StompCommand.SUBSCRIBE, "/topic/channel1", "test-message");
        Message<?> result = this.channelInterceptor.preSend(message, null);
        Assert.assertNotNull(result);
    }

    @Test
    public void testPreSendWithEmptyDestination() {

        // Drop all requests
        this.configuration.addTransportStompInterceptor(
              message -> null,
              EnumSet.allOf(StompCommand.class),
              new AnyDestinationMatcher(),
              1);

        Message<?> result = this.channelInterceptor.preSend(
              generateMessage(StompCommand.SUBSCRIBE, "", "test-message"),
              null);
        Assert.assertNotNull(result);

        result = this.channelInterceptor.preSend(
              generateMessage(StompCommand.SUBSCRIBE, null, "test-message"),
              null);
        Assert.assertNotNull(result);
    }

    @Test
    public void testPreSendWithMultipleInterceptors() {

        this.configuration.addTransportStompInterceptor(
              new AppendMessageInterceptor("interceptor1"),
              EnumSet.of(StompCommand.SUBSCRIBE),
              new AnyDestinationMatcher(),
              100);

        this.configuration.addTransportStompInterceptor(
              new AppendMessageInterceptor("interceptor2"),
              EnumSet.of(StompCommand.SUBSCRIBE),
              new StartsWithDestinationMatcher("/topic/channel2"),
              50);

        // Shouldn't be executed for channel2
        this.configuration.addTransportStompInterceptor(
              new AppendMessageInterceptor("interceptor3"),
              EnumSet.allOf(StompCommand.class),
              new StartsWithDestinationMatcher("/topic/channel3"),
              1);

        // Shouldn't be executed for SUBSCRIBE messages
        this.configuration.addTransportStompInterceptor(
              new AppendMessageInterceptor("interceptor4"),
              EnumSet.of(StompCommand.SEND),
              new AnyDestinationMatcher(),
              1);

        List<String> payload = new ArrayList<>();
        payload.add("message");

        Message<?> result = this.channelInterceptor.preSend(
              generateMessage(StompCommand.SUBSCRIBE, "/topic/channel2", payload),
              null);

        Assert.assertNotNull(result);
        Assert.assertEquals(payload.size(), 3);
        Assert.assertEquals(payload.get(0), "message");
        Assert.assertEquals(payload.get(1), "interceptor2");
        Assert.assertEquals(payload.get(2), "interceptor1");
    }

    @Test
    public void testPreSendDropMessageWithMultipleInterceptors() {

        this.configuration.addTransportStompInterceptor(
              message -> {
                  throw new RuntimeException("This interceptors should never be executed");
              },
              EnumSet.of(StompCommand.SUBSCRIBE),
              new AnyDestinationMatcher(),
              30);

        this.configuration.addTransportStompInterceptor(
              new AppendMessageInterceptor("interceptor2"),
              EnumSet.of(StompCommand.SUBSCRIBE),
              new StartsWithDestinationMatcher("/topic/channel2"),
              10);

        this.configuration.addTransportStompInterceptor(
              message -> null,
              EnumSet.of(StompCommand.SUBSCRIBE),
              new StartsWithDestinationMatcher("/topic/channel2"),
              20);


        List<String> payload = new ArrayList<>();
        payload.add("message");

        Message<?> result = this.channelInterceptor.preSend(
              generateMessage(StompCommand.SUBSCRIBE, "/topic/channel2", payload),
              null);

        Assert.assertNull(result);
    }

    private static class AppendMessageInterceptor implements TransportStompInterceptor {

        private String messageToAppend;

        public AppendMessageInterceptor(String messageToAppend) {
            this.messageToAppend = messageToAppend;
        }

        @Override
        public Message<?> preSend(Message<?> message) {
            ((List<String>) message.getPayload()).add(messageToAppend);
            return message;
        }
    }

    private <T> Message<T> generateMessage(StompCommand command, String destination, T payload) {
        MessageBuilder<T> messageBuilder = MessageBuilder.withPayload(payload);
        messageBuilder.setHeader("simpDestination", destination)
              .setHeader("stompCommand", command);
        return messageBuilder.build();
    }
}
