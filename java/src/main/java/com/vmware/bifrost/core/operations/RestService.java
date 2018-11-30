/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.operations;

import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.util.RestControllerInvoker;
import com.vmware.bifrost.core.util.URIMatcher;
import com.vmware.bifrost.core.util.URIMethodResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * RestService is responsible for handling UI Rest requests. It operates in two modes:
 *
 * 1. As a simple REST client that translates a RestOperation object into a Rest Call to an external URI
 * 2. As a dispatch engine that checks if there are any RestController instances that serve the requested URI.
 *    If there is a match, the method arguments are extracted from all the meta data provided via annotations to the
 *    method, and then calls the method using the correct sequence of arguments, with the correct types, in the correct
 *    order.
 *
 * //TODO: This service will eventually be called from any class implementing AbstractService via the bus
 *
 * @see com.vmware.bifrost.core.model.RestOperation
 */
@Service
public class RestService extends Loggable {

    private final RestTemplate restTemplate;


    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private RestControllerInvoker controllerInvoker;

    @Autowired
    public RestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    /**
     * If calling the service via DI, then make the requested Rest Request locally via controller, or externally
     * via a standard rest call.
     *
     * @param operation
     * @param <Req> request body type
     * @param <Resp> return body type
     */
    public <Req, Resp> void restServiceRequest(RestOperation<Req, Resp> operation) {

        // check if the URI is local to the system
        URIMethodResult methodResult = locateRestControllerForURIAndMethod(operation);
        if(methodResult != null && methodResult.getMethod() != null) {
            invokeRestController(methodResult, operation);
            return;
        }


        HttpEntity<Req> entity = null;
        HttpHeaders headers = null;

        // check if headers are set.
        if (operation != null && operation.getHeaders() != null) {
            headers = new HttpHeaders();

            for (String key : operation.getHeaders().keySet()) {
                headers.add(key, operation.getHeaders().get(key));
            }
        }

        try {
            if (headers != null) {
                entity = new HttpEntity<>(operation.getBody(), headers);
            } else {
                entity = new HttpEntity<>(operation.getBody());
            }
        } catch (NullPointerException npe) {
            if (headers != null) {
                entity = new HttpEntity<>(headers);
            }
        }

        try {
            ResponseEntity resp;
            switch (operation.getMethod()) {
                case GET:
                    operation.getSuccessHandler().accept(
                            (Resp)this.restTemplate.getForObject(
                                    operation.getUri(),
                                    Class.forName(operation.getApiClass()
                                    )
                            )
                    );
                    break;

                case POST:
                    resp = this.restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.POST,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;

                case PUT:
                    resp = this.restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.PUT,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;

                case PATCH:
                    resp = this.restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.PATCH,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp)resp.getBody());
                    break;

                case DELETE:
                    resp = this.restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.DELETE,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;
            }

        } catch (ClassNotFoundException exp) {

            this.logErrorMessage("Class not found when making rest call", exp.getMessage());

        } catch (HttpClientErrorException cee) {

            this.logErrorMessage("Client Error when making rest call", cee.getMessage());

        } catch (HttpServerErrorException see) {

            this.logErrorMessage("Server Error when making rest call", see.getMessage());

        } catch (NullPointerException npe) {

            this.logErrorMessage("Null Pointer Exception when making rest call", npe.getMessage());
        }


    }

    private URIMethodResult locateRestControllerForURIAndMethod(RestOperation operation) {

        URIMethodResult result = URIMatcher.findControllerMatch(
                context,
                operation.getUri(),
                RequestMethod.valueOf(operation.getMethod().toString())
        );

        if (result != null) {
            this.logDebugMessage("Located handling method for URI: "
                    + operation.getUri().getRawPath(), result.getMethod().getName());
        } else {
            this.logDebugMessage("Unable to locate a local handler for for URI: ", operation.getUri().getRawPath());
        }
        return result;
    }

    private void invokeRestController(URIMethodResult result, RestOperation operation) {
        try {
            controllerInvoker.invokeMethod(result, operation);

        } catch (RuntimeException rexp) {

            // do something here.
        }
    }

}
