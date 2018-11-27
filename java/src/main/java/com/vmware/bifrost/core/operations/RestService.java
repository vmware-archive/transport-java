package com.vmware.bifrost.core.operations;

import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.core.model.RestOperation;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Copyright(c) VMware Inc. 2018
 */


@Service
public class RestService extends Loggable {

    private final RestTemplate restTemplate;
    private final RestTemplateBuilder builder;

    public RestService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.builder = restTemplateBuilder;
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

            switch (operation.getMethod()) {
                case GET:
                    operation.getSuccessHandler().accept(
                            (Resp) this.restTemplate.getForObject(operation.getUri(), Class.forName(operation.getApiClass()))
                    );
                    break;

                case POST:
                    operation.getSuccessHandler().accept(
                            (Resp) this.restTemplate.exchange(operation.getUri(), HttpMethod.POST, entity, Class.forName(operation.getApiClass()))
                    );

                case PUT:
                    operation.getSuccessHandler().accept(
                            (Resp) this.restTemplate.exchange(operation.getUri(), HttpMethod.PUT, entity, Class.forName(operation.getApiClass()))
                    );

                case PATCH:
                    operation.getSuccessHandler().accept(
                            (Resp) this.restTemplate.exchange(operation.getUri(), HttpMethod.PATCH, entity, Class.forName(operation.getApiClass()))
                    );

                case DELETE:
                    operation.getSuccessHandler().accept(
                            (Resp) this.restTemplate.exchange(operation.getUri(), HttpMethod.DELETE, entity, Class.forName(operation.getApiClass()))
                    );
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
