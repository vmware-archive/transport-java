package com.vmware.bifrost.broker.nats;

import com.vmware.bifrost.broker.MessageBrokerSubscription;
import io.nats.client.Dispatcher;
import lombok.AccessLevel;
import lombok.Getter;

@Getter(AccessLevel.PACKAGE)
public class NatsSubscription extends MessageBrokerSubscription {
    final Dispatcher dispatcher;

    NatsSubscription(final Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }
}
