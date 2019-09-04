/*
 * Copyright(c) VMware Inc. 2017-2018
 */
package com.vmware.bifrost.bridge.spring.controllers;

import com.vmware.bifrost.bridge.RequestException;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.core.util.Loggable;
import com.vmware.bifrost.bus.EventBus;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import com.vmware.bifrost.bridge.Request;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.Principal;
import java.util.UUID;

@Controller
public class MessageController extends Loggable {

    private EventBus bus;

    @Autowired
    MessageController(EventBus eventBus) {
        this.bus = eventBus;
    }

    @MessageMapping("/{topicDestination}")
    public void bridgeMessage(Request request, @DestinationVariable String topicDestination) throws RequestException {
        validateRequest(request);
        this.logTraceMessage("New inbound message received for channel: ", topicDestination);
        if (bus.isGalacticChannel(topicDestination)) {
            // unwrap the payload and forward it to the external message broker
            bus.sendRequestMessage(topicDestination, request.getPayload());
        } else {
            bus.sendRequestMessage(topicDestination, request);
        }
    }

    @MessageMapping("/queue/{queueDestination}")
    public void bridgeQueueMessage(Request request,
                                   @DestinationVariable String queueDestination,
                                   Principal principal) throws RequestException {

        validateRequest(request);
        request.setTargetUser(principal.getName());
        this.logTraceMessage("New inbound message received for private channel: ", queueDestination);
        bus.sendRequestMessageToTarget(queueDestination, request, request.getId(), principal.getName());
    }

    @MessageExceptionHandler
    public Response handleException(Throwable exception) {
        this.logErrorMessage("New inbound message received cannot be processed", exception.getMessage());
        Response resp = new Response(UUID.randomUUID(), true);
        resp.setErrorCode(400);
        resp.setErrorMessage("Request cannot be processed: " + exception.getMessage());
        return resp;
    }

    @ExceptionHandler({RequestException.class, OnErrorNotImplementedException.class})
    public Response handleRequestException(Throwable exception) {
        return this.handleException(exception);
    }

    private void validateRequest(Request request) throws RequestException {
        if(request.getId() == null) {
            throw new RequestException("Request 'id' is missing");
        } else if(request.getRequest() == null) {
            throw new RequestException("Request 'request' is missing");
        } else if(request.getVersion() == null) {
            throw new RequestException("Request 'version' is missing");
        }
    }
}
