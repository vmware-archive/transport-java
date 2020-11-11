/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core;


import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.UUID;

@BifrostService
@Component
public class TestService extends AbstractService<TestRequest, TestResponse> {

    public TestService() {
        super("test::TestService");
    }

    @Override
    protected void handleServiceRequest(TestRequest request, Message message) {
        switch (request.getRequest()) {
            case TestCommand.COMMAND_A:
                this.handleCommandA(request, message.getId());
                break;

            case TestCommand.COMMAND_B:
                this.handleCommandB(request, message.getId());
                break;

            case TestCommand.COMMAND_C:
                this.handleCommandC(request, message.getId());
                break;

            case TestCommand.COMMAND_OVERQUEUE:
                this.handleCommandOverQueue(request, message.getId());
                break;

            case TestCommand.ERROR_RESPONSE_OVERQUEUE:
                this.sendErrorResponseOverQueue(request, message.getId());
                break;

            case TestCommand.GENERAL_ERROR_OVERQUEUE:
                this.sendGeneralErrorOverQueue(request, message.getId());
                break;
        }
    }

    private void handleCommandA(TestRequest request, UUID id) {

        TestServiceObjectRequest requestPayload = this.castPayload(TestServiceObjectRequest.class, request);
        TestServiceObjectResponse responsePayload = new TestServiceObjectResponse();
        responsePayload.setResponseValue("CommandA-" + requestPayload.getRequestValue());

        TestResponse resp = new TestResponse(request.getId(), responsePayload);

        this.sendResponse(resp, id);

    }

    private void handleCommandB(TestRequest request, UUID id) {

        TestServiceObjectRequest requestPayload = this.castPayload(TestServiceObjectRequest.class, request);
        TestServiceObjectResponse responsePayload = new TestServiceObjectResponse();
        responsePayload.setResponseValue("CommandB-" + requestPayload.getRequestValue());

        TestResponse resp = new TestResponse(request.getId(), responsePayload);

        this.sendResponse(resp, id);


    }

    private void handleCommandC(TestRequest request, UUID id) {

        TestServiceObjectResponse responsePayload = new TestServiceObjectResponse();

        try {
            this.restServiceRequest(
                    UUID.randomUUID(),
                    request.uri,
                    request.method,
                    request.getPayload(),
                    null,
                    String.class.getName(),
                    (Response<String> response) -> {
                        responsePayload.setResponseValue(response.getPayload());
                        this.sendResponse(
                                new TestResponse(request.getId(), responsePayload),
                                id
                        );
                    },
                    (Response<RestError> error) -> {
                        this.sendError(error, id);
                    }
            );
        } catch (Exception exp) {
            this.sendError(new RestError("something went wrong making rest request", 500), id);
        }
    }

    private void handleCommandOverQueue(TestRequest request, UUID id) {
        TestServiceObjectRequest requestPayload = this.castPayload(TestServiceObjectRequest.class, request);
        TestServiceObjectResponse responsePayload = new TestServiceObjectResponse();
        responsePayload.setResponseValue("CommandOverQueue-" + requestPayload.getRequestValue());

        TestResponse resp = new TestResponse(request.getId(), responsePayload);

        this.sendResponse(resp, id, request.getTargetUser());
    }

    private void sendErrorResponseOverQueue(TestRequest request, UUID id) {
        GeneralError err = new GeneralError();
        err.message = "error";
        Response<GeneralError> errResponse = new Response<>(id, err);
        this.sendError(errResponse, id, request.getTargetUser());
    }

    private void sendGeneralErrorOverQueue(TestRequest request, UUID id) {
        GeneralError err = new GeneralError(HttpStatus.I_AM_A_TEAPOT.getReasonPhrase(), HttpStatus.I_AM_A_TEAPOT.toString());
        this.sendError(err, id, request.getTargetUser());
    }
}
