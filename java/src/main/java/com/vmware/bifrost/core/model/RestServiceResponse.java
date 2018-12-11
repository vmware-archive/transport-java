/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.model;

import com.vmware.bifrost.bridge.Response;

import java.util.UUID;

public class RestServiceResponse<Payld> extends Response {

    public RestServiceResponse(Integer version, UUID uuid, Payld payload) {
        super(version, uuid, payload);
    }

    public RestServiceResponse(UUID uuid, Payld payload) {
        super(uuid, payload);
    }

    public RestServiceResponse(Integer version, UUID uuid, boolean error) {
        super(version, uuid, error);
    }

    public RestServiceResponse(UUID uuid, boolean error) {
        super(uuid, error);
    }
}
