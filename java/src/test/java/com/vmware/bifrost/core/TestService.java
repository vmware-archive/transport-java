/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core;


import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.core.model.*;
import org.springframework.stereotype.Component;

@BifrostService
@Component
public class TestService extends AbstractService<TestRequest, TestResponse>{

    public TestService() {
        super("test::TestService");
    }

    @Override
    protected void handleServiceRequest(TestRequest request) {
        switch (request.getCommand()){
            case TestCommand.COMMAND_A:
                this.handleCommandA(request);
                break;

            case TestCommand.COMMAND_B:
                this.handleCommandB(request);
                break;
        }
    }

    private void handleCommandA(TestRequest request) {

        TestServiceObjectRequest requestPayload = this.castPayload(TestServiceObjectRequest.class, request);
        TestServiceObjectResponse responsePayload = new TestServiceObjectResponse();
        responsePayload.setResponseValue("CommandA-" + requestPayload.getRequestValue());

        TestResponse resp = new TestResponse(request.getId(), responsePayload);

        this.sendResponse(resp);

    }

    private void handleCommandB(TestRequest request) {

        TestServiceObjectRequest requestPayload = this.castPayload(TestServiceObjectRequest.class, request);
        TestServiceObjectResponse responsePayload = new TestServiceObjectResponse();
        responsePayload.setResponseValue("CommandB-" + requestPayload.getRequestValue());

        TestResponse resp = new TestResponse(request.getId(), responsePayload);

        this.sendResponse(resp);


    }
}
