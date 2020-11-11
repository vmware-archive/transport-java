/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.transport.core.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

/**
 * Used by restServiceRequest in AbstractService.
 * @param <Req> The request payload type (what we're sending)
 * @param <Resp> the response payload type (what we're expecting back)
 */
public class RestServiceRequest<Req, Resp> {

    @Getter @Setter
    private URI uri;

    @Getter @Setter
    private HttpMethod method;

    @Getter @Setter
    private Req body;

    @Getter @Setter
    private String apiClass;

    @Getter @Setter
    private Resp responseType;

    @Getter @Setter
    private Map<String,String> headers;

    @Getter @Setter
    private String sentFrom;

}
