package com.vmware.bifrost.core.autogen;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import lombok.Getter;
import lombok.Setter;

/**
 * Implementation of the IApiType interface
 *
 * @param <RequestType> The service request object
 * @param <ResponseType> The service response object
 */
public class ApiType<RequestType extends Request, ResponseType extends Response> implements IApiType<RequestType, ResponseType> {
    @Getter @Setter
    private RequestType requestType;
    @Getter @Setter
    private ResponseType responseType;

    public ApiType(RequestType requestType, ResponseType responseType) {
        this.requestType = requestType;
        this.responseType = responseType;
    }
}
