/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.interceptors;

import com.vmware.transport.bridge.spring.config.interceptors.TransportStompInterceptor;
import com.vmware.transport.core.util.Loggable;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

public class MessageLoggerInterceptor extends Loggable implements TransportStompInterceptor {

    private String msg;

    public MessageLoggerInterceptor(String msg) {
        this.msg = msg;
    }

    @Override
    public Message<?> preSend(Message<?> message) {

        StompHeaderAccessor header = StompHeaderAccessor.wrap(message);
        String destination = header.getDestination();

        this.logInfoMessage(
              "\uD83D\uDCE4",
              msg + destination,
              message.toString());

        return message;
    }
}
