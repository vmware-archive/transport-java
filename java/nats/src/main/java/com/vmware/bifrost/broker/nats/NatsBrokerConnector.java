/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.vmware.bifrost.broker.nats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.broker.GalacticMessageHandler;
import com.vmware.bifrost.broker.MessageBrokerConnector;
import com.vmware.bifrost.core.util.Loggable;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import org.apache.commons.lang.StringUtils;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class NatsBrokerConnector extends Loggable
        implements MessageBrokerConnector<NatsChannelConfig, NatsSubscription> {
    private final String brokerId;
    private final Connection natsConnection;
    private final ObjectMapper objectMapper;

    public NatsBrokerConnector(Connection natsConnection) {
        this.brokerId = "NatsBroker-" + UUID.randomUUID().toString();
        this.natsConnection = natsConnection;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getMessageBrokerId() {
        return brokerId;
    }

    @Override
    public NatsSubscription subscribeToChannel(NatsChannelConfig channelConfig, GalacticMessageHandler handler) {
        if (StringUtils.isEmpty(channelConfig.getSubject())) {
            return null;
        }

        Dispatcher dispatcher = this.natsConnection.createDispatcher((msg) -> {
            // if channel is dedicated only to sending messages to the broker, don't allow out-bound
            // messages from reaching the UI by self-feeding the echoed messages to the channel.
            if (channelConfig.getChannelMode() != ChannelMode.PublishOnly) {
                handler.onMessage(new String(msg.getData()));
            }
        });

        dispatcher.subscribe(channelConfig.getSubject());
        return new NatsSubscription(dispatcher);
    }

    @Override
    public boolean unsubscribeFromChannel(NatsSubscription subscription) {
        if (subscription != null && subscription.getDispatcher() != null &&
                subscription.getDispatcher().isActive()) {
            try {
                CompletableFuture<Boolean> drained = subscription.getDispatcher().drain(Duration.ofSeconds(10));
                drained.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error(e.getMessage());
            }

            return true;
        }
        return false;
    }

    @Override
    public boolean sendMessage(NatsChannelConfig channelConfig, Object payload) {
        // if channelConfig is empty, or no subject is provided, do nothing
        // and signal that sendMessage failed
        if (channelConfig == null ||
            StringUtils.isEmpty(channelConfig.getSubject())) {
            return false;
        }

        // if the channel is purposed to listen to messages only, do nothing.
        if (channelConfig.getChannelMode() == ChannelMode.ListenOnly) {
            log.warn("Message on a listen-only channel '{}' is ignored.\n", channelConfig.getSubject());
            return true;
        }

        try {
            byte[] payloadBytes = objectMapper.writer().writeValueAsBytes(payload);
            this.natsConnection.publish(channelConfig.getSubject(), payloadBytes);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public void connectMessageBroker() {
        // NOOP
    }

    @Override
    public void disconnectMessageBroker() {
        // drain connections
        try {
            CompletableFuture<Boolean> drained = this.natsConnection.drain(Duration.ofSeconds(10));
            drained.get();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error(e.getMessage());
        }

    }

    // create a new channel config that allows bi-directional communication between java and
    // the broker. this means sending a message to the channel using Bifrost API will immediately
    // cause the message to be echoed back on its own channel.
    public NatsChannelConfig newDuplexGalacticChannel(String subject) {
        return new NatsChannelConfig(getMessageBrokerId(), subject, ChannelMode.BiDirectional);
    }

    // create a new channel config that will only be allowed to send messages to the broker
    // but incoming messages from the broker will be ignored
    public NatsChannelConfig newPublishOnlyGalacticChannel(String subject) {
        return new NatsChannelConfig(getMessageBrokerId(), subject, ChannelMode.PublishOnly);
    }

    // create a new channel config that will only be allowed to listen to messages from the broker
    // meaning that any message sent from java will be ignored
    public NatsChannelConfig newListenOnlyGalacticChannel(String subject) {
        return new NatsChannelConfig(getMessageBrokerId(), subject, ChannelMode.ListenOnly);
    }
}