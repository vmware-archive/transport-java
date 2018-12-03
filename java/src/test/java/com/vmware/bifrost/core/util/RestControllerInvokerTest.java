/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.operations.MockRestController;
import com.vmware.bifrost.core.operations.SampleDTO;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Parameter;
import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        MockRestController.class,
        RestControllerInvoker.class,
        RestControllerReflection.class,
        DefaultParameterNameDiscoverer.class,
        URIMatcher.class

})
public class RestControllerInvokerTest {

    @Autowired
    private RestControllerInvoker invoker;


    @Autowired
    private URIMatcher uriMatcher;

    @Test
    public void testInvokeMethodSimplest() throws Exception {

        URI uri = new URI("/foo");

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.GET);

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

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.GET);

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

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.GET);

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

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError error) -> {
                    assertThat(error.message).isEqualTo("Method requires request param 'someQuery', This maps to method argument 'bozQuery', but wasn't supplied with URI properties.");
                    assertThat(error.errorCode).isEqualTo(500);
                }
        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMethodNormalMissingAllQueryParams() throws Exception {

        URI uri = new URI("/foo/someValue/bar/123");

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError error) -> {
                    assertThat(error.message).isEqualTo("Method requires request parameters, however none have been supplied.");
                    assertThat(error.errorCode).isEqualTo(500);
                }
        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeMultiMethodURIGet() throws Exception {

        URI uri = new URI("/multi");

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.GET);

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

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.POST);

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

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.PATCH);

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

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.PATCH);

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


    @Test
    public void testInvokePatchMapping() throws Exception {

        URI uri = new URI("/patch-mapping/630360ba-fb59-4571-b08d-2d5f219691de");

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.PATCH);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.PATCH);

        SampleDTO dto = new SampleDTO();
        dto.setName("Melody is");
        dto.setValue(0.5);

        operation.setBody(dto);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("patchMappingWithParams-630360ba-fb59-4571-b08d-2d5f219691de-Melody is:0.5", response);
                }

        );

        invoker.invokeMethod(result, operation);
    }


    @Test
    public void testInvokeGetMapping() throws Exception {

        UUID randId = UUID.randomUUID();
        URI uri = new URI("/get-mapping/" + randId.toString());

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.GET);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("getMappingWithParams-" + randId.toString(), response);
                }

        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokeDeleteMapping() throws Exception {

        UUID randId = UUID.randomUUID();
        URI uri = new URI("/delete-mapping/" + randId.toString());

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.DELETE);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.DELETE);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("deleteMappingWithParams-" + randId.toString(), response);
                }

        );

        invoker.invokeMethod(result, operation);
    }

    @Test
    public void testInvokePutMapping() throws Exception {

        UUID randId = UUID.randomUUID();
        URI uri = new URI("/put-mapping/" + randId.toString());

        URIMethodResult result = uriMatcher.findControllerMatch(uri, RequestMethod.PUT);

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(uri);
        operation.setMethod(HttpMethod.PUT);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("putMappingWithParams-" + randId.toString(), response);
                }

        );

        invoker.invokeMethod(result, operation);
    }



}
