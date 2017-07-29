package com.vmware.bifrost.bridge.spring.handlers;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/*
 * Copyright(c) VMware Inc. 2017
 */

public class BridgeHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        System.out.println("****&&&*** SHOPS ---- " + message.getPayload());
    }

}


