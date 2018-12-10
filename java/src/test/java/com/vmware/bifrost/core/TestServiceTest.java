/*
 * Copyright(c) VMware Inc. 2018
 */

package com.vmware.bifrost.core;

import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.EventBusImpl;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.model.*;
import com.vmware.bifrost.core.operations.MockRestController;
import com.vmware.bifrost.core.operations.RestService;
import com.vmware.bifrost.core.operations.SecurityConfiguration;
import com.vmware.bifrost.core.util.RestControllerInvoker;
import com.vmware.bifrost.core.util.RestControllerReflection;
import com.vmware.bifrost.core.util.ServiceMethodLookupUtil;
import com.vmware.bifrost.core.util.URIMatcher;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        SecurityConfiguration.class,
        RestService.class,
        MockRestController.class,
        RestControllerInvoker.class,
        DefaultParameterNameDiscoverer.class,
        RestControllerReflection.class,
        ServiceMethodLookupUtil.class,
        URIMatcher.class,
        RestTemplate.class,
        TestService.class,
        EventBusImpl.class
})
public class TestServiceTest {

    @Autowired
    EventBus bus;

    @Autowired
    ApplicationContext context;

    @Test
    public void checkChannelIsSet() {

        TestService service = context.getBean(TestService.class);
        Assert.assertEquals("test::TestService", service.getServiceChannel());

    }


    @Test
    public void testServiceCommandA() {

        String serviceChannel = "test::TestService";

        TestServiceObjectRequest requestPayload = new TestServiceObjectRequest();
        requestPayload.setRequestValue("My Little Melody");

        TestRequest request = new TestRequest();
        UUID id = UUID.randomUUID();
        request.setId(id);
        request.setCommand(TestCommand.COMMAND_A);
        request.setPayload(requestPayload);

        bus.requestOnce(
                serviceChannel,
                request,
                (Message msg) -> {
                    TestResponse resp = (TestResponse)msg.getPayload();
                    TestServiceObjectResponse respPayload = resp.getPayload();
                    Assert.assertEquals(id, resp.getId());
                    Assert.assertEquals("CommandA-My Little Melody", respPayload.getResponseValue());
                }
        );
    }

    @Test
    public void testServiceCommandB() {

        String serviceChannel = "test::TestService";

        TestServiceObjectRequest requestPayload = new TestServiceObjectRequest();
        requestPayload.setRequestValue("My Little Song");

        TestRequest request = new TestRequest();
        UUID id = UUID.randomUUID();
        request.setId(id);
        request.setCommand(TestCommand.COMMAND_B);
        request.setPayload(requestPayload);

        bus.requestOnce(
                serviceChannel,
                request,
                (Message msg) -> {
                    TestResponse resp = (TestResponse)msg.getPayload();
                    TestServiceObjectResponse respPayload = resp.getPayload();
                    Assert.assertEquals(id, resp.getId());
                    Assert.assertEquals("CommandB-My Little Song", respPayload.getResponseValue());
                }
        );
    }


    @Test
    public void testErrorHandlingWithBadPayload() {

        String serviceChannel = "test::TestService";

        bus.requestOnce(
                serviceChannel,
                "Some String",
                (Message msg) -> {
                    // should not fire
                    Assert.fail();
                },
                (Message msg) -> {
                    Response resp = (Response)msg.getPayload();
                    Assert.assertEquals("TestService cannot handle request, "
                            + "payload isn't derived from 'Request', type: String", resp.getErrorMessage());
                }
        );
    }

}
