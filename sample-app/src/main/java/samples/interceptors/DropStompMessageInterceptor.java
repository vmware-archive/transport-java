/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.interceptors;

import com.vmware.transport.bridge.spring.config.interceptors.TransportStompInterceptor;
import com.vmware.transport.core.util.Loggable;
import org.springframework.messaging.Message;

public class DropStompMessageInterceptor extends Loggable implements TransportStompInterceptor {

    private int dropEveryXMessage;

    private int messageCount = 0;

    public DropStompMessageInterceptor(int dropEveryXMessage) {
        this.dropEveryXMessage = dropEveryXMessage;
    }

    @Override
    public Message<?> preSend(Message<?> message) {
        this.messageCount++;
        if (this.messageCount % this.dropEveryXMessage == 0) {
            logWarnMessage("[DropMessageInterceptor] Dropped message: " + message.toString());
            return null;
        }

        return message;
    }
}
