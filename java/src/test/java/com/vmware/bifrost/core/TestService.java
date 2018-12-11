/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core;


import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.*;
import org.springframework.stereotype.Component;

import java.util.UUID;

@BifrostService
@Component
public class TestService extends AbstractService<TestRequest, TestResponse>{

    public TestService() {
        super("test::TestService");
    }

    @Override
    protected void handleServiceRequest(TestRequest request, Message message) {
        switch (request.getCommand()){
            case TestCommand.COMMAND_A:
                this.handleCommandA(request, message.getId());
                break;

            case TestCommand.COMMAND_B:
                this.handleCommandB(request, message.getId());
                break;

            case TestCommand.COMMAND_C:
                this.handleCommandC(request, message.getId());
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

        this.restServiceRequest(
                request.uri,
                request.method,
                request.getPayload(),
                null,
                String.class.getName(),
                (String apiResponse) -> {
                    responsePayload.setResponseValue(apiResponse);
                    this.sendResponse(
                            new TestResponse(request.getId(), responsePayload),
                            id
                    );
                },
                (RestError error) -> {
                    TestResponse resp = new TestResponse(request.getId(), responsePayload, true);
                    resp.setErrorCode(error.errorCode);
                    resp.setErrorMessage(error.message);
                    this.sendError(resp, id);
                }
        );
    }
}
