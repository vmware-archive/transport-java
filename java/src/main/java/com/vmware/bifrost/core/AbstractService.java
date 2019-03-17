/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core;

import com.fasterxml.jackson.databind.MapperFeature;
import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestServiceRequest;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@SuppressWarnings("unchecked")
public abstract class AbstractService<RequestType extends Request, ResponseType extends Response>
        extends AbstractBase {

    private String serviceChannel;

    public AbstractService(String serviceChannel) {
        super();
        this.serviceChannel = serviceChannel;
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }

    String getServiceChannel() {
        return this.serviceChannel;
    }

    /**
     * Initialize Service to start listening for request messages on the service channel.
     * Errors will be ignored, as they would be
     */
    public void initialize() {

        this.bus.listenRequestStream(this.serviceChannel,
                (Message message) -> {
                    try {

                        this.logInfoMessage(
                                "\uD83D\uDCE5",
                                "Service Request Received",
                                message.getPayload().toString());

                        this.handleServiceRequest((RequestType) message.getPayload(), message);

                    } catch (ClassCastException cce) {
                        this.logErrorMessage("Service unable to process request, " +
                                "request cannot be cast", message.getPayload().getClass().getSimpleName());

                        GeneralError error = new GeneralError(
                                this.getClass().getSimpleName()
                                        + " cannot handle request, payload isn't derived from 'Request', type: "
                                        + message.getPayload().getClass().getSimpleName(),
                                cce,
                                500
                        );
                        this.sendError(error, message.getId());
                    }
                },
                (Message message) -> {
                    // ignore error, it will be handled by service consumer.
                }
        );

        this.logInfoMessage("\uD83D\uDCE3", this.getClass().getSimpleName()
                + " initialized, handling requests on channel", this.serviceChannel);
        // this.methodLookupUtil.loadCustomHandlers();
    }

    protected abstract void handleServiceRequest(RequestType request, Message busMessage) throws Exception;

    protected void sendResponse(ResponseType response, UUID id) {
        this.logInfoMessage(
                "\uD83D\uDCE4",
                "Sending Service Response",
                response.toString());
        this.bus.sendResponseMessageWithId(this.serviceChannel, response, id);
    }

    protected <E extends GeneralError> void sendError(E error, UUID id) {
        this.bus.sendErrorMessageWithId(this.serviceChannel, error, id);
    }

    <T> T castPayload(Class clazz, Request request) throws ClassCastException {
        return (T) this.mapper.convertValue(request.getPayload(), clazz);
    }
}

