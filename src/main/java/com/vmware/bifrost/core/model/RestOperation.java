/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.model;

import com.vmware.bifrost.core.error.RestError;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Used By RestService to convert an incoming request into a RestOperation.
 * @param <Payld> The Request Payload Type (what is being sent)
 * @param <Resp> The Response Payload Type (what we're expecting back)
 */
public class RestOperation<Payld, Resp> {

    public RestOperation() {
        // set content type to application/json by default
        headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
    }

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
