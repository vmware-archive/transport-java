/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.operations.MockRestController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        MockRestController.class,
        RestControllerInvoker.class

})
public class RestControllerInvokerTest {

    @Autowired
    private RestControllerInvoker invoker;

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    public void testInvokeMethodSimplest() throws Exception {

        URI uri = new URI("/foo");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) -> {
                    assertThat(response).isEqualTo("FooBarSimplest");
                }
        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMethodSimple() throws Exception {

        URI uri = new URI("/foo/melody?boz=isAPrettyBaby");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) -> {
                    assertThat(response).isEqualTo("FooBarSimple:/foo/melody?boz=isAPrettyBaby");
                }
        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMethodNormal() throws Exception {

        URI uri = new URI("/foo/someValue/bar/123?someQuery=hello&anotherQuery=goodbye");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) -> {
                    assertThat(response).isEqualTo("FooBarNormal:/foo/someValue/bar/123?someQuery=hello&anotherQuery=goodbye");
                }
        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMethodNormalMissingQueryParam() throws Exception {

        URI uri = new URI("/foo/someValue/bar/123?anotherQuery=goodbye");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError error) -> {
                    assertThat(error.message).isEqualTo("Method requires request param 'someQuery', This maps to method argument 'bozQuery', but wasn't supplied with URI properties.");
                    assertThat(error.status).isEqualTo("REST Error: Invalid Request Parameters");
                }
        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMethodNormalMissingAllQueryParams() throws Exception {

        URI uri = new URI("/foo/someValue/bar/123");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError error) -> {
                    assertThat(error.message).isEqualTo("Method requires request parameters, however none have been supplied.");
                    assertThat(error.status).isEqualTo("REST Error: Missing Request Parameters");
                }
        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMultiMethodURIGet() throws Exception {

        URI uri = new URI("/multi");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("multiMethodURIGet", response);
                }

        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMultiMethodURIPost() throws Exception {

        URI uri = new URI("/multi");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.POST);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.POST);
        operation.setBody("SplishySplashy");
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("multiMethodURIPost-SplishySplashy", response);
                }

        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMultiMethodURIPatch() throws Exception {

        URI uri = new URI("/multi?query=naughtyPuppy");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.PATCH);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.PATCH);
        operation.setBody("Ember");
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("multiMethodURIPatch-naughtyPuppy-Ember", response);
                }

        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMultiMethodURIPatchNoQuery() throws Exception {

        URI uri = new URI("/multi");

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri, RequestMethod.PATCH);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.PATCH);
        operation.setBody("Ember");
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("multiMethodURIPatch-null-Ember", response);
                }

        );

        invoker.invokeMethod(result, operation);
    }

}
