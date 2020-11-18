/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bridge.spring.TransportEnabled;
import com.vmware.transport.bridge.spring.TransportService;
import com.vmware.transport.bus.EventBus;
import com.vmware.transport.bus.model.Message;
import com.vmware.transport.bus.store.BusStoreApi;
import com.vmware.transport.bus.store.model.BusStore;
import com.vmware.transport.core.error.GeneralError;
import com.vmware.transport.core.error.RestError;
import com.vmware.transport.core.model.RestOperation;
import com.vmware.transport.core.model.RestServiceRequest;
import com.vmware.transport.core.util.Loggable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpMethod;


import java.net.URI;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/*
 * Copyright(c) VMware Inc. 2019
 */
@TransportService
@SuppressWarnings("unchecked")
public abstract class AbstractBase extends Loggable implements TransportEnabled {

    @Autowired
    protected EventBus bus;

    @Autowired
    protected BusStoreApi storeManager;

    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected ResourceLoader resourceLoader;

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
     */
    protected <Req, Resp> void restServiceRequest(
            UUID id,
            URI uri,
            HttpMethod method,
            Req payload,
            Map<String, String> headers,
            String responseApiClass,
            Consumer<Response<Resp>> successHandler,
            Consumer<Response<RestError>> errorHandler
    ) {

        BusStore<String, Map<String, String>> serviceWideHeadersStore =
                storeManager.createStore(CoreStores.ServiceWideHeaders); // createStore is safe, guarantees no NPE.
        if (serviceWideHeadersStore.get(getName()) == null) {
            serviceWideHeadersStore.put(getName(), new HashMap<>(), CoreStoreStates.ServiceHeadersUpdated);
        }

        Map serviceHeadersMap = serviceWideHeadersStore.get(getName());
        Map<String, String> mergedHeaders = new HashMap<>();

        // if provided headers is not empty, merge values from it into mergedHeaders
        if (headers != null) {
            for (String key : headers.keySet()) {
                mergedHeaders.merge(key, headers.get(key), (v, v2) -> v2);
            }
        }

        // apply service-wide headers
        for (Object key : serviceHeadersMap.keySet()) {
            mergedHeaders.merge((String) key, (String) serviceHeadersMap.get(key), (v, v2) -> v2);
        }

        // set defaults
        RestServiceRequest req = new RestServiceRequest();
        req.setApiClass(responseApiClass);
        req.setMethod(method);
        req.setUri(uri);
        req.setBody(payload);
        req.setSentFrom(this.getName());
        req.setHeaders(mergedHeaders);

        Request request = new Request<Req>();
        request.setId(id);
        request.setHeaders(mergedHeaders);
        request.setPayload(req);
        request.setRequest(method.toString());
        request.setChannel(CoreChannels.RestService);
        request.setCreated(Calendar.getInstance().getTime());

        callService(
                id,
                CoreChannels.RestService,
                request,
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
     * @deprecated use method with RestOperation.
     */
    @Deprecated
    protected <Req, Resp> void restServiceRequest(
            URI uri,
            HttpMethod method,
            Req payload,
            Map<String, String> headers,
            String responseApiClass,
            Consumer<Response<Resp>> successHandler,
            Consumer<Response<RestError>> errorHandler
    )  throws Exception {
        this.restServiceRequest(UUID.randomUUID(), uri, method, payload, headers, responseApiClass, successHandler, errorHandler);
    }

    /**
     * Make a new RestService call.
     *
     * @param operation RestOperation for call Encapsulates individual argument calls.
     */
    protected void restServiceRequest(RestOperation operation) {

        this.restServiceRequest(
                operation.getId(),
                operation.getUri(),
                operation.getMethod(),
                operation.getBody(),
                operation.getHeaders(),
                operation.getApiClass(),
                response -> operation.getSuccessHandler().accept(response.getPayload()),
                restErrorResponse -> operation.getErrorHandler().accept(restErrorResponse.getPayload())
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
    protected <Req, Resp, Err> void callService(
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

    /**
     * Set global headers for the implementation class. This means, once the class invokes
     * a REST call through restServiceRequest, the headers set within this class will be
     * applied to the request.
     * @param headersMap
     */
    public void setHeaders(Map<String, String> headersMap) {
        BusStore<String, Map<String, String>> busStore = storeManager.getStore(CoreStores.ServiceWideHeaders);
        busStore.put(getName(), headersMap, CoreStoreStates.ServiceHeadersUpdated);
    }

	/**
     * Override global host and port settings for all REST calls via REST Service. This is useful if your
     * API specification driven autogenerated code is trying to call the URI and port defined in the spec.
     * That URI may conflict with localhost if you're running inside a container.
     *
     * Use this when you need to override the target port or host for all API calls.
     *
     * @param host the host you want to set, i.e 'mynew.host.com'
     * @param port the port you want to set i.e. '9999'
     */
    protected void setGlobalRestServiceHostOptions(String host, String port) {
        BusStore<String, String> baseHostStore = this.bus.getStoreManager().getStore(CoreStores.RestServiceHostConfig);
        if (host != null) {
            baseHostStore.put(CoreStoreKeys.RestServiceBaseHost, host, null);
        }
         if (port != null) {
             baseHostStore.put(CoreStoreKeys.RestServiceBasePort, port, null);
         }
    }

}
