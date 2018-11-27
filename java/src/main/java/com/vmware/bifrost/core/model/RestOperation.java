/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.model;

import com.vmware.bifrost.core.error.RestError;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class RestOperation<T> {

    @Getter @Setter
    private UUID id;

    @Getter @Setter
    private URI uri;

    @Getter @Setter
    private HttpRequest method;

    @Getter @Setter
    private Object body;

    @Getter @Setter
    private Map<String,String> headers;

    @Getter @Setter
    private String sentFrom;

    @Getter @Setter
    private Consumer<T> successHandler;

    @Getter @Setter
    private Consumer<RestError> errorHandler;
}