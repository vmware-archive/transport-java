/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.model;

import com.vmware.bifrost.bridge.Request;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;

public class RestServiceRequest<Req, Resp> extends Request<Req> {

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
