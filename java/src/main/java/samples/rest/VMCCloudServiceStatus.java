/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */

package samples.rest;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

/**
 * Sample service that makes calls to VMW Cloud Services status API.
 */
@Component
public class VMCCloudServiceStatus extends AbstractService<Request<String>, Response<CloudServicesStatusResponse>> {

    VMCCloudServiceStatus() {
        super("services-CloudServiceStatus");
    }

    @Override
    protected void handleServiceRequest(Request<String> request, Message busMessage) throws Exception {
       this.handleCloudServiceStatusRequest(request);
    }

    private void handleCloudServiceStatusRequest(Request req) throws Exception {

        // create a rest call for cloud services.
        RestOperation<String, CloudServicesStatusResponse> restOperation = new RestOperation<>();
        restOperation.setId(UUID.randomUUID());
        restOperation.setUri(new URI("https://status.vmware-services.io/api/v2/status.json"));
        restOperation.setApiClass("CloudServicesStatusResponse");
        restOperation.setMethod(HttpMethod.GET);
        restOperation.setSentFrom(this.getName());

        // define success handler for rest call.
        restOperation.setSuccessHandler(
                (CloudServicesStatusResponse resp) -> {
                    Response<CloudServicesStatusResponse> response = new Response<>(req.getId(), resp);
                    this.sendResponse(response, req.getId());
                }
        );

        // define an error handler for rest call.
        restOperation.setErrorHandler(
                (RestError error) -> {
                    this.sendError(error, req.getId());
                }
        );

        // make rest call
        super.restServiceRequest(restOperation);

    }
}
