/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.model;

import com.vmware.transport.bridge.Response;
import java.util.UUID;

public class TestResponse extends Response<TestServiceObjectResponse> {

    public TestResponse(Integer version, UUID uuid, TestServiceObjectResponse payload) {
        super(version, uuid, payload);
    }

    public TestResponse(UUID uuid, TestServiceObjectResponse payload) {
        super(uuid, payload);
    }

    public TestResponse(UUID uuid, TestServiceObjectResponse payload, boolean error) {
        super(uuid, payload);
        this.error = error;
    }

    public TestResponse(Integer version, UUID uuid, boolean error) {
        super(version, uuid, error);
    }

    public TestResponse(UUID uuid, boolean error) {
        super(uuid, error);
    }
}
