package com.vmware.transport.core.model;

import com.vmware.transport.bridge.Request;
import org.springframework.http.HttpMethod;

import java.net.URI;

/*
 * Copyright(c) VMware Inc. 2018
 */
public class TestRequest extends Request<TestServiceObjectRequest> {
    public URI uri;
    public HttpMethod method;
}
