package com.vmware.bifrost.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestServiceRequest;
import com.vmware.bifrost.core.util.Loggable;
import com.vmware.bifrost.core.util.ServiceMethodLookupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/*
 * Copyright(c) VMware Inc. 2019
 */
@BifrostService
@SuppressWarnings("unchecked")
public abstract class AbstractBase extends Loggable implements BifrostEnabled {

    @Autowired
    protected EventBus bus;

    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected ResourceLoader resourceLoader;

    @Autowired
    protected ServiceMethodLookupUtil methodLookupUtil;

    protected ObjectMapper mapper = new ObjectMapper();

    /**
     * Make a new RestService call.
     * @param id the UUID you'd like to use for this call.
     * @param uri URI you would like to call
     * @param method HTTP Method to use (of type HttpMethod), GET, POST, PATCH etc.
     * @param payload Payload to send as body of request.
     * @param headers HTTP headers to send.
     * @param responseApiClass the class type of the response object you're expecting back from the API
     * @param successHandler success handler lambda to handle API response.
     * @param errorHandler error handler lambda to handle response (RestError)
     * @param <Req> Type of the payload being sent.
     * @param <Resp> Type of the response being returned.
     */
    protected <Req, Resp> void restServiceRequest(
            UUID id,
            URI uri,
            HttpMethod method,
            Req payload,
            Map<String, String> headers,
            String responseApiClass,
            Consumer<Resp> successHandler,
            Consumer<RestError> errorHandler
    ) {

        // set defaults
        RestServiceRequest req = new RestServiceRequest();
        req.setApiClass(responseApiClass);
        req.setMethod(method);
        req.setUri(uri);
        req.setBody(payload);
        req.setSentFrom(this.getName());
        req.setHeaders(headers);

        this.bus.requestOnceWithId(
                id,
                CoreChannels.RestService,
                req,
                (Message message) -> successHandler.accept((Resp) message.getPayload()),
                (Message message) -> errorHandler.accept((RestError) message.getPayload())
        );
    }

    /**
     * Make a new RestService call.
     * @param uri URI you would like to call
     * @param method HTTP Method to use (of type HttpMethod), GET, POST, PATCH etc.
     * @param payload Payload to send as body of request.
     * @param headers HTTP headers to send.
     * @param responseApiClass the class type of the response object you're expecting back from the API
     * @param successHandler success handler lambda to handle API response.
     * @param errorHandler error handler lambda to handle response (RestError)
     * @param <Req> Type of the payload being sent.
     * @param <Resp> Type of the response being returned.
     */
    protected <Req, Resp> void restServiceRequest(
            URI uri,
            HttpMethod method,
            Req payload,
            Map<String, String> headers,
            String responseApiClass,
            Consumer<Resp> successHandler,
            Consumer<RestError> errorHandler
    ) {
        this.restServiceRequest(UUID.randomUUID(), uri, method, payload, headers, responseApiClass, successHandler, errorHandler);
    }
}
