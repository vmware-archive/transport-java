/*
 * Copyright(c) VMware Inc. 2018 - 2019
 */
package com.vmware.bifrost.core;

import com.fasterxml.jackson.databind.MapperFeature;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.core.interfaces.BusServiceEnabled;

import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("unchecked")
public abstract class AbstractService<RequestType extends Request, ResponseType extends Response>
        extends AbstractBase implements BusServiceEnabled {

    protected String serviceChannel;
    protected BusTransaction serviceChannelStream;

    public AbstractService(String serviceChannel) {
        super();
        this.serviceChannel = serviceChannel;
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }

    String getServiceChannel() {
        return this.serviceChannel;
    }

    /**
     * Build an error response based on request rejection details.
     * @param requestType Request
     * @return Response
     */
    private Response<GeneralError> buildErrorResponse(RequestType requestType) {
        if (!requestType.getRejected()) {
            return null;
        }

        Response<GeneralError> errorResponse = null;

        try {
            GeneralError generalError = mapper.readValue(
                    requestType.getPayload().toString(),
                    GeneralError.class);
            errorResponse = new Response<>(
                    requestType.getId(), generalError);
            errorResponse.setErrorCode(generalError.errorCode);
            errorResponse.setError(true);
            errorResponse.setErrorMessage(generalError.message);
        } catch (IOException e) {
            logWarnMessage("Failed to parse request payload into GeneralError: " + e.getMessage());
        }

        return errorResponse;
    }

    /**
     * Initialize Service to start listening for request messages on the service channel.
     * Errors will be ignored, as they would be
     */
    public void initialize() {
        this.online();
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

    protected void handleUnknownRequest(Request request) {
        String unknownRequest = this.getName() + ": Unknown Request/Command '" + request.getRequest() + "'";
        Response<String> response = new Response<>(request.getId(), unknownRequest);
        this.logInfoMessage(
                "\uD83D\uDCE4",
                "Sending Service Response (Unknown Request)",
                response.toString());
        this.bus.sendResponseMessageWithId(this.serviceChannel, response, request.getId());
    }

    public void online() {

        this.serviceChannelStream = this.bus.listenRequestStream(this.serviceChannel,
                (Message message) -> {
                    try {

                        this.logInfoMessage(
                                "\uD83D\uDCE5",
                                "Service Request Received",
                                message.getPayload().toString());

                        // if request is rejected by the interceptor, isRejected will be true and the payload will
                        // be an instance of GeneralError. send the error response straight back to the user
                        RequestType requestType = (RequestType) message.getPayload();
                        Response<GeneralError> requestError = buildErrorResponse(requestType);

                        if (requestError != null) {
                            this.bus.sendErrorMessageWithId(serviceChannel, requestError, requestType.getId());
                            return;
                        }

                        this.handleServiceRequest(requestType, message);

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
                        cce.printStackTrace();

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

    public void offline() {
        this.serviceChannelStream.unsubscribe();
    }

}

