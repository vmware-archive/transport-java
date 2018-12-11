/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.operations;

import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.AbstractService;
import com.vmware.bifrost.core.CoreChannels;
import com.vmware.bifrost.core.model.RestServiceRequest;
import com.vmware.bifrost.core.model.RestServiceResponse;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.util.RestControllerInvoker;
import com.vmware.bifrost.core.util.URIMatcher;
import com.vmware.bifrost.core.util.URIMethodResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.*;

import java.util.function.Consumer;

/**
 * RestService is responsible for handling UI Rest requests. It operates in two modes:
 * <p>
 * 1. As a simple REST client that translates a RestOperation object into a Rest Call to an external URI
 * 2. As a dispatch engine that checks if there are any RestController instances that serve the requested URI.
 * If there is a match, the method arguments are extracted from all the meta data provided via annotations to the
 * method, and then calls the method using the correct sequence of arguments, with the correct types, in the correct
 * order.
 * <p>
 * //TODO: This service will eventually be called from any class implementing AbstractService via the bus
 *
 * @see com.vmware.bifrost.core.model.RestOperation
 */
@Service
@SuppressWarnings("unchecked")
public class RestService extends AbstractService<RestServiceRequest, RestServiceResponse> {

    private final URIMatcher uriMatcher;
    private final RestControllerInvoker controllerInvoker;

    @Autowired
    public RestService(URIMatcher uriMatcher, RestControllerInvoker controllerInvoker) {
        super(CoreChannels.RestService);
        this.uriMatcher = uriMatcher;
        this.controllerInvoker = controllerInvoker;
    }

    /**
     * Handle bus request.
     *
     * @param request RestServiceRequest instance sent on bus
     */
    @Override
    protected void handleServiceRequest(RestServiceRequest request, Message message) {

        this.logDebugMessage(this.getClass().getSimpleName()
                + " handling Rest Request for URI: " + request.getUri().toASCIIString());

        RestOperation operation = new RestOperation();
        operation.setUri(request.getUri());
        operation.setBody(request.getBody());
        operation.setMethod(request.getMethod());
        operation.setHeaders(request.getHeaders());
        operation.setApiClass(request.getApiClass());
        operation.setId(request.getId());
        operation.setSentFrom(this.getName());

        // create a success handler to respond
        Consumer<Object> successHandler = (Object restResponseObject) -> {
            this.logDebugMessage(this.getClass().getSimpleName()
                    + " Successful REST response " + request.getUri().toASCIIString());
            RestServiceResponse response = new RestServiceResponse(request.getId(), restResponseObject);
            this.sendResponse(response, message.getId());
        };

        operation.setSuccessHandler(successHandler);

        // create an error handler to respond in case something goes wrong.
        Consumer<RestError> errorHandler = (RestError error) -> {
            this.logErrorMessage(this.getClass().getSimpleName()
                    + " Error with making REST response ", request.getUri().toASCIIString());
            this.sendError(error, message.getId());
        };

        operation.setErrorHandler(errorHandler);

        try {
            this.restServiceRequest(operation);

        } catch (Exception exp) {

            // something bubbled up, throw it back as a response.
            this.logErrorMessage(this.getName()
                    + " Exception thrown when making REST response ", request.getUri().toASCIIString());

            RestError error = new RestError("Exception thrown '"
                    + exp.getClass().getSimpleName() + ": " + exp.getMessage() + "'", 500);
            this.sendError(error, message.getId());

        }
    }

    /**
     * If calling the service via DI, then make the requested Rest Request locally via controller, or externally
     * via a standard rest call.
     *
     * @param operation RestOperation to be supplied
     * @param <Req>     request body type
     * @param <Resp>    return body type
     */
    <Req, Resp> void restServiceRequest(RestOperation<Req, Resp> operation) throws Exception {

        // check if the URI is local to the system
        URIMethodResult methodResult = locateRestControllerForURIAndMethod(operation);
        if (methodResult != null && methodResult.getMethod() != null) {
            invokeRestController(methodResult, operation);
            return;
        }

        HttpEntity<Req> entity;
        HttpHeaders headers = new HttpHeaders();

        // fix patch issue.
        MediaType mediaType = new MediaType("application", "merge-patch+json");

        // check if headers are set.
        if (operation.getHeaders() != null) {

            for (String key : operation.getHeaders().keySet()) {
                headers.add(key, operation.getHeaders().get(key));
            }
        }
        headers.setContentType(mediaType);
        entity = new HttpEntity<>(operation.getBody(), headers);

        // required because PATCH causes a freakout.
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);

        try {
            ResponseEntity resp;
            switch (operation.getMethod()) {
                case GET:
                    resp = restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.GET,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;

                case POST:
                    resp = restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.POST,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;

                case PUT:
                    resp = restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.PUT,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;

                case PATCH:
                    resp = restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.PATCH,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;

                case DELETE:
                    resp = restTemplate.exchange(
                            operation.getUri(),
                            HttpMethod.DELETE,
                            entity,
                            Class.forName(operation.getApiClass())
                    );
                    operation.getSuccessHandler().accept((Resp) resp.getBody());
                    break;
            }

        } catch (RestClientException exp) {

            this.logErrorMessage("REST Client Error, unable to complete request: ", exp.getMessage());
            operation.getErrorHandler().accept(
                    new RestError("REST Client Error, unable to complete request: "
                            + exp.getMessage(), 500)
            );

        } catch (NullPointerException npe) {

            this.logErrorMessage("Null Pointer Exception when making REST Call", npe.toString());
            operation.getErrorHandler().accept(
                    new RestError("Null Pointer exception thrown for: "
                            + operation.getUri().toString(), 500)
            );
        }

    }

    private URIMethodResult locateRestControllerForURIAndMethod(RestOperation operation) throws Exception {

        URIMethodResult result = uriMatcher.findControllerMatch(
                operation.getUri(),
                RequestMethod.valueOf(operation.getMethod().toString())
        );

        if (result != null) {
            this.logDebugMessage("Located handling method for URI: "
                    + operation.getUri().getRawPath(), result.getMethod().getName());
        } else {
            this.logDebugMessage("Unable to locate a local handler for for URI: ",
                    operation.getUri().getRawPath());
        }
        return result;
    }

    private void invokeRestController(URIMethodResult result, RestOperation operation) {

        controllerInvoker.invokeMethod(result, operation);

    }
}
