package com.vmware.bifrost.core.autogen;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.error.RestError;


/**
 * This the interface function callback from the ApiBridge after a failed API call.
 *
 * @param <RequestType> The service request object
 * @param <ResponseType>The service response object
 */
public interface IApiFailureHandler<RequestType extends Request, ResponseType extends Response> {
    void apiFailureHandler(IApiType<RequestType, ResponseType> apiObject, RestError err, Message message);
}
