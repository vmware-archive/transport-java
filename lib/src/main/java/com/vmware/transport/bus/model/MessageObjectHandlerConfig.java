/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import lombok.Getter;
import lombok.Setter;

public class MessageObjectHandlerConfig<T> extends MessageObject<T> {

    @Setter
    @Getter
    private String sendChannel;

    @Setter
    @Getter
    private String returnChannel;

    @Setter
    @Getter
    private boolean singleResponse;

    public MessageObjectHandlerConfig() {
        super();
    }

    public MessageObjectHandlerConfig(MessageType type, T payload) {
        super(type, payload);
    }
}
