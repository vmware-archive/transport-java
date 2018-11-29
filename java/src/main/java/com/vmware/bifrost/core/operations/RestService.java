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

    public URIMethodResult locateRestControllerForURIAndMethod(RestOperation operation) {

        URIMethodResult result = URIMatcher.findControllerMatch(
                context,
                operation.getUri(),
                RequestMethod.valueOf(operation.getMethod().toString())
        );

        if (result != null) {
            this.logDebugMessage("Located handling method for URI: "
                    + operation.getUri().getRawPath(), result.getMethod().getName());
        } else {
            this.logDebugMessage("Unable to locate a local handler for for URI: "
                    + operation.getUri().getRawPath(), result.getMethod().getName());
        }
        return result;
    }

    public void invokeRestController(URIMethodResult result, RestOperation operation) {
        try {
            controllerInvoker.invokeMethod(result, operation);
        } catch (RuntimeException rexp) {

            // do something here.
        }
    }


    public <Req, Resp> void restServiceRequest(RestOperation<Req, Resp> operation) {

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

}
