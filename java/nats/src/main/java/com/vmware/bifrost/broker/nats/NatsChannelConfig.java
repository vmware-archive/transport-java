package com.vmware.bifrost.broker.nats;

import com.vmware.bifrost.broker.GalacticChannelConfig;
import lombok.AccessLevel;
import lombok.Getter;

enum ChannelMode {
    PublishOnly,
    ListenOnly,
    BiDirectional
}

@Getter(AccessLevel.PACKAGE)
public class NatsChannelConfig extends GalacticChannelConfig {
    private final String subject;
    private final ChannelMode channelMode;

    public NatsChannelConfig(String messageBrokerId, String subject, ChannelMode channelMode) {
        super(messageBrokerId);

        this.subject = subject;
        this.channelMode = channelMode;
    }
}
