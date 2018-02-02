package com.vmware.bifrost.bridge.spring.controllers;

import com.vmware.bifrost.bridge.RequestException;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bridge.util.BifrostUtil;
import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.bus.MessagebusService;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import com.vmware.bifrost.bridge.Request;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.UUID;


@Controller
public class MessageController extends Loggable {

    @Autowired
    private MessagebusService bus;

    @MessageMapping("/{topicDestination}")
    public void bridgeMessage(Request request, @DestinationVariable String topicDestination) throws RequestException {

        valiateRequest(request);
        this.logTraceMessage("New inbound message received for", topicDestination);
        bus.sendRequest(BifrostUtil.convertTopicToChannel(topicDestination), request);

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

    private void valiateRequest(Request request) throws RequestException {
        if(request.getId() == null) {
            throw new RequestException("Request 'id' is missing");
        } else if(request.type == null) {
            throw new RequestException("Request 'type' is missing");
        } else if(request.getVersion() == null) {
            throw new RequestException("Request 'version' is missing");
        }
    }

}
