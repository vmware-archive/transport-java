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
import com.vmware.bifrost.core.model.RestServiceResponse;
import com.vmware.bifrost.core.util.ClassMapper;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.UUID;

/**
 * Sample service that makes calls to VMW Cloud Services status API.
 */
@Component
public class VMWCloudServiceStatus extends AbstractService<Request<String>, Response<CloudServicesStatusResponse>> {

    VMWCloudServiceStatus() {
        super("services-CloudServiceStatus");
    }

    @Override
    protected void handleServiceRequest(Request<String> request, Message busMessage) throws Exception {

        // there is only a single operation for this service. We can ignore all requests and auto run our
        // one request handler.
        this.handleCloudServiceStatusRequest(request);
    }

    private void handleCloudServiceStatusRequest(Request req) throws Exception {

        // make rest call
        super.restServiceRequest(
                UUID.randomUUID(),
                new URI("https://status.vmware-services.io/api/v2/status.json"),
                HttpMethod.GET,
                null, null,
                "samples.rest.CloudServicesStatusResponse",
                (Response<CloudServicesStatusResponse> resp) -> {
                    this.sendResponse(resp, req.getId());
                },
                (Response<RestError> error) -> {
                    this.sendError(error, req.getId());
                }
        );
    }
}
