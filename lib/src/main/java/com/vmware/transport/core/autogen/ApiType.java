/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.autogen;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

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
    @Getter @Setter
    private Map<String, String> apiHeaders;

    public ApiType(RequestType requestType, ResponseType responseType) {
        this.requestType = requestType;
        this.responseType = responseType;
        this.apiHeaders = new HashMap<>();
    }
}
