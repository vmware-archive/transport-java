package com.vmware.bifrost.core.autogen;


import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;

/**
 * This the interface function callback from the ApiBridge after a successful API call. Payload is a stringified JSON object.
 *
 * @param <RequestType> The service request object
 * @param <ResponseType>The service response object
 */
public interface IApiSuccessHandler<RequestType extends Request, ResponseType extends Response> {
    void apiSuccessHandler(IApiType<RequestType, ResponseType> apiType, String payload, Message message);
}
