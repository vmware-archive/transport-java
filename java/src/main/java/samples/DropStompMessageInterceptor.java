/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples;

import com.vmware.bifrost.bridge.spring.config.interceptors.BifrostStompInterceptor;
import com.vmware.bifrost.core.util.Loggable;
import org.springframework.messaging.Message;

public class DropStompMessageInterceptor extends Loggable implements BifrostStompInterceptor {

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
