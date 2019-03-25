package com.vmware.bifrost.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.core.model.RestOperation;
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
     *
     * @param id               the UUID you'd like to use for this call.
     * @param uri              URI you would like to call
     * @param method           HTTP Method to use (of type HttpMethod), GET, POST, PATCH etc.
     * @param payload          Payload to send as body of request.
     * @param headers          HTTP headers to send.
     * @param responseApiClass the class type of the response object you're expecting back from the API
     * @param successHandler   success handler lambda to handle API response.
     * @param errorHandler     error handler lambda to handle response (RestError)
     * @param <Req>            Type of the payload being sent.
     * @param <Resp>           Type of the response being returned.
     * @param <Err>            Type of error being returned
     */
    protected <Req, Resp, Err extends GeneralError> void restServiceRequest(
            UUID id,
            URI uri,
            HttpMethod method,
            Req payload,
            Map<String, String> headers,
            String responseApiClass,
            Consumer<Resp> successHandler,
            Consumer<Err> errorHandler
    ) {

        // set defaults
        RestServiceRequest req = new RestServiceRequest();
        req.setApiClass(responseApiClass);
        req.setMethod(method);
        req.setUri(uri);
        req.setBody(payload);
        req.setSentFrom(this.getName());
        req.setHeaders(headers);

        callService(
                id,
                CoreChannels.RestService,
                req,
                successHandler,
                errorHandler);
    }

    /**
     * Make a new RestService call.
     *
     * @param uri              URI you would like to call
     * @param method           HTTP Method to use (of type HttpMethod), GET, POST, PATCH etc.
     * @param payload          Payload to send as body of request.
     * @param headers          HTTP headers to send.
     * @param responseApiClass the class type of the response object you're expecting back from the API
     * @param successHandler   success handler lambda to handle API response.
     * @param errorHandler     error handler lambda to handle response (RestError)
     * @param <Req>            Type of the payload being sent.
     * @param <Resp>           Type of the response being returned.
     * @param <Err>            Type of the error being returned.
     * @deprecated use method with RestOperation.
     */
    protected <Req, Resp, Err extends GeneralError> void restServiceRequest(
            URI uri,
            HttpMethod method,
            Req payload,
            Map<String, String> headers,
            String responseApiClass,
            Consumer<Resp> successHandler,
            Consumer<Err> errorHandler
    )  throws Exception {
        this.restServiceRequest(UUID.randomUUID(), uri, method, payload, headers, responseApiClass, successHandler, errorHandler);
    }

    /**
     * Make a new RestService call.
     *
     * @param operation RestOperation for call Encapsulates individual argument calls.
     * @param <Req>     Type of the payload being sent.
     * @param <Resp>    Type of the response being returned.
     */
    protected <Req, Resp> void restServiceRequest(RestOperation<Req, Resp> operation) {

        this.restServiceRequest(
                operation.getId(),
                operation.getUri(),
                operation.getMethod(),
                operation.getBody(),
                operation.getHeaders(),
                operation.getApiClass(),
                operation.getSuccessHandler(),
                operation.getErrorHandler()
        );
    }

    /**
     * Call a service on a channel with a supplied payload, handle success and error response.\
     * @param uuid the UUID you would like to use for the service call.
     * @param channelName the channel you want to call
     * @param request the request to be sent.
     * @param successHandler the lambda you want to be passed the successful result
     * @param errorHandler the lambda you want to be passed any errors
     * @param <Req> generic type of the request
     * @param <Resp> generic type of the response\
     */
    protected <Req, Resp, Err extends GeneralError> void callService(
            UUID uuid,
            String channelName,
            Req request,
            Consumer<Resp> successHandler,
            Consumer<Err> errorHandler
    ) {

        this.bus.requestOnceWithId(
                uuid,
                channelName,
                request,
                (Message message) -> successHandler.accept((Resp) message.getPayload()),
                (Message message) -> errorHandler.accept((Err)message.getPayload())
        );
    }

    /**
     * Call a service on a channel with a supplied payload, handle success and error response.\
     * @param channelName the channel you want to call
     * @param request the request to be sent.
     * @param successHandler the lambda you want to be passed the successful result
     * @param errorHandler the lambda you want to be passed any errors
     * @param <Req> generic type of the request
     * @param <Resp> generic type of the response
     */
    protected <Req, Resp> void callService(
            String channelName,
            Req request,
            Consumer<Resp> successHandler,
            Consumer<GeneralError> errorHandler
    ) {

       callService(UUID.randomUUID(), channelName, request, successHandler, errorHandler);
    }
}
