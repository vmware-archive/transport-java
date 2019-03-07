/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.controllers;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.RequestException;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.EventBusImpl;
import com.vmware.bifrost.bus.model.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class MessageControllerTest {

    private EventBus bus;

    private MessageController controller;

    private Message message;
    private int count;

    @Before
    public void before() throws Exception {
        this.bus = new EventBusImpl();
        this.controller = new MessageController(bus);

        this.message = null;
        this.count = 0;
    }

    @Test
    public void testBridgeMessageWithInvalidRequest() {
        Exception ex = null;
        try {
            this.controller.bridgeMessage(new Request(), "channel1");
        } catch (Exception e) {
            ex = e;
        }

        assertRequestException(ex, "Request 'id' is missing");

        ex = null;
        try {
            this.controller.bridgeMessage(new Request(UUID.randomUUID(), null, null), "channel1");
        } catch (Exception e) {
            ex = e;
        }
        assertRequestException(ex, "Request 'command' is missing");

        ex = null;
        try {
            Request request = new Request();
            request.setRequest("command");
            request.setId(UUID.randomUUID());
            this.controller.bridgeMessage(request, "channel1");
        } catch (Exception e) {
            ex = e;
        }
        assertRequestException(ex, "Request 'version' is missing");
    }

    @Test
    public void testBridgeMessageWithValidRequest() throws Exception {
        this.bus.listenRequestStream("channel", message -> {
            this.message = message;
            this.count++;
        });

        Request bridgeRequest = new Request(UUID.randomUUID(), "command", "request-payload");

        this.controller.bridgeMessage(bridgeRequest, "channel");
        Assert.assertEquals(this.count, 1);
        Assert.assertEquals(this.message.getPayload(), bridgeRequest);
    }

    @Test
    public void testHandleException() {
        Response response = this.controller.handleException(new Exception("request-exception"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getErrorCode(), 400);
        Assert.assertEquals(response.getErrorMessage(), "Request cannot be processed: request-exception");
    }

    @Test
    public void testHandleRequestException() {
        Response response = this.controller.handleRequestException(new Exception("request-exception"));
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getErrorCode(), 400);
        Assert.assertEquals(response.getErrorMessage(), "Request cannot be processed: request-exception");
    }

    private void assertRequestException(Exception ex, String expectedErrorMsg) {
        Assert.assertTrue(ex instanceof RequestException);
        Assert.assertEquals(ex.getMessage(), expectedErrorMsg);
    }
}
