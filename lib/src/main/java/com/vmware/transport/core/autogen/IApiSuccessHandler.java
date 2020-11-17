package com.vmware.transport.core.autogen;


import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.model.Message;

/**
 * This the interface function callback from the ApiBridge after a successful API call. Payload is a stringified JSON object.
 *
 * @param <RequestType> The service request object
 * @param <ResponseType>The service response object
 */
public interface IApiSuccessHandler<RequestType extends Request, ResponseType extends Response> {
    void apiSuccessHandler(IApiType<RequestType, ResponseType> apiType, Object payload, Message message);
}
