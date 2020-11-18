/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.model;

import com.vmware.transport.bridge.Request;
import org.springframework.http.HttpMethod;

import java.net.URI;

public class TestRequest extends Request<TestServiceObjectRequest> {
    public URI uri;
    public HttpMethod method;
}
