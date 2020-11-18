/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.autogen;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;

import java.util.Map;

/**
 * An ApiType is an opaque package that can be passed between the Service layer and the API layer.
 * It is typically passed to an API class from a service, which then passes it to the ApiBridge which
 * performs the actual API request to a remote service, and then modifies it for passing back to the original
 * service with the response from the API call.
 */
public interface IApiType<RequestType extends Request, ResponseType extends Response> {
    RequestType getRequestType();
    ResponseType getResponseType();
    Map<String, String> getApiHeaders();
}
