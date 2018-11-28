/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.operations.MockResponseA;
import com.vmware.bifrost.core.operations.MockRestController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.junit4.SpringRunner;

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

        URIMethodResult result = URIMatcher.findControllerMatch(context, uri);

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

//    @Test
//    public void testInvokeMethodSimple() throws Exception {
//
//        URI uri = new URI("/foo/melody");
//
//        URIMethodResult result = URIMatcher.findControllerMatch(context, uri);
//
//        RestOperation<Object, String> operation = new RestOperation<>();
//        operation.setApiClass(String.class.getName());
//        operation.setUri(uri);
//        operation.setMethod(HttpMethod.GET);
//        operation.setSuccessHandler(
//                (String response) -> {
//                    assertThat(response).isEqualTo("FooBarSimple-melody");
//                }
//        );
//
//        invoker.invokeMethod(result, operation);
//    }

}
