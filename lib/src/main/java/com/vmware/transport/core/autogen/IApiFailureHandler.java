/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.autogen;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.core.error.RestError;

/**
 * This the interface function callback from the ApiBridge after a failed API call.
 *
 * @param <RequestType> The service request object
 * @param <ResponseType>The service response object
 */
public interface IApiFailureHandler<RequestType extends Request, ResponseType extends Response> {
    void apiFailureHandler(IApiType<RequestType, ResponseType> apiObject, RestError err, Message message);
}
