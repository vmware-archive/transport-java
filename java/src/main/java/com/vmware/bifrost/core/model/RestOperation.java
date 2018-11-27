/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.model;

import com.vmware.bifrost.core.error.RestError;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RestOperation<Payld, Resp> {

    @Getter @Setter
    private UUID id;

    @Getter @Setter
    private URI uri;

    @Getter @Setter
    private HttpMethod method;

    @Getter @Setter
    private Payld body;

    @Getter @Setter
    private String apiClass;

    @Getter @Setter
    private Map<String,String> headers;

    @Getter @Setter
    private String sentFrom;

    @Getter @Setter
    private Consumer<Resp> successHandler;

    @Getter @Setter
    private Consumer<RestError> errorHandler;
}