package com.vmware.bifrost.core.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.util.RestControllerInvoker;
import com.vmware.bifrost.core.util.URIMethodResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        RestService.class,
        MockRestController.class,
        RestControllerInvoker.class
})
public class RestServiceTest {

    @Autowired
    private RestService restService;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestControllerInvoker invoker;


    private MockResponseA buildMockResponseA() {
        MockResponseA mockResponseA = new MockResponseA();
        mockResponseA.setName("Prettiest Baby");
        mockResponseA.setValue("Melody");
        return mockResponseA;
    }

    private MockResponseB buildMockResponseB() {
        MockResponseB mockResponseB = new MockResponseB();
        mockResponseB.setId(UUID.randomUUID());
        mockResponseB.setValue("Pizza");
        return mockResponseB;
    }

    private String mapResponseToSting(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }


    private void configureMockRestServer(String uri, String response) {
        this.server.expect(manyTimes(), requestTo(uri))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGet() throws Exception {

        configureMockRestServer(
                "/user/1",
                mapResponseToSting(buildMockResponseA())
        );

        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());
        operation.setUri(new URI("/user/1"));
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (MockResponseA response) -> {
                    assertThat(response.getName()).isEqualTo("Prettiest Baby");
                    assertThat(response.getValue()).isEqualTo("Melody");
                }
        );

        restService.restServiceRequest(operation);

    }

    @Test
    public void testPost() throws Exception {

        MockResponseB mock = buildMockResponseB();

        configureMockRestServer(
                "/user",
                mapResponseToSting(mock)
        );

        RestOperation<Object, MockResponseB> operation = new RestOperation<>();
        operation.setApiClass(MockResponseB.class.getName());
        operation.setUri(new URI("/user"));
        operation.setMethod(HttpMethod.POST);
        operation.setSuccessHandler(
                (MockResponseB response) -> {
                    assertThat(response.getId()).isEqualTo(mock.getId());
                    assertThat(response.getValue()).isEqualTo("Pizza");
                }
        );

        restService.restServiceRequest(operation);

    }

    @Test
    public void testPatch() throws Exception {

        MockResponseB mock = buildMockResponseB();

        configureMockRestServer(
                "/user",
                mapResponseToSting(mock)
        );

        RestOperation<Object, MockResponseB> operation = new RestOperation<>();
        operation.setApiClass(MockResponseB.class.getName());
        operation.setUri(new URI("/user"));
        operation.setMethod(HttpMethod.PATCH);
        operation.setSuccessHandler(
                (MockResponseB response) -> {
                    assertThat(response.getId()).isEqualTo(mock.getId());
                    assertThat(response.getValue()).isEqualTo("Pizza");
                }
        );

        restService.restServiceRequest(operation);

    }

    @Test
    public void testPut() throws Exception {

        MockResponseB mock = buildMockResponseB();

        configureMockRestServer(
                "/user",
                mapResponseToSting(mock)
        );

        RestOperation<Object, MockResponseB> operation = new RestOperation<>();
        operation.setApiClass(MockResponseB.class.getName());
        operation.setUri(new URI("/user"));
        operation.setMethod(HttpMethod.PUT);
        operation.setSuccessHandler(
                (MockResponseB response) -> {
                    assertThat(response.getId()).isEqualTo(mock.getId());
                    assertThat(response.getValue()).isEqualTo("Pizza");
                }
        );

        restService.restServiceRequest(operation);

    }

    @Test
    public void testDelete() throws Exception {

        configureMockRestServer(
                "/user",
                mapResponseToSting(buildMockResponseA())
        );

        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());
        operation.setUri(new URI("/user"));
        operation.setMethod(HttpMethod.DELETE);
        operation.setSuccessHandler(
                (MockResponseA response) -> {
                    assertThat(response.getName()).isEqualTo("Prettiest Baby");
                    assertThat(response.getValue()).isEqualTo("Melody");
                }
        );

        restService.restServiceRequest(operation);

    }


    @Test
    public void testUriMappings() throws Exception {


        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());
        operation.setUri(new URI("/foo/bar"));
        operation.setMethod(HttpMethod.GET);

        URIMethodResult result = restService.locateRestControllerForURIAndMethod(operation);
        Assert.assertNotNull(result);

    }


    @Test
    public void testLocalMethodInvokeSimple() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/foo/bar?boz=baz"));
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) ->
                    Assert.assertEquals("FooBarSimple:/foo/bar?boz=baz", response)
        );

        URIMethodResult result = restService.locateRestControllerForURIAndMethod(operation);
        restService.invokeRestController(result, operation);

    }

    @Test
    public void testLocalMethodInvokeNormal() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/foo/someVar/bar/123?someQuery=something&anotherQuery=nothing"));
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) ->
                        Assert.assertEquals(
                                "FooBarNormal:/foo/someVar/bar/123?someQuery=something&anotherQuery=nothing",
                                response
                        )
        );

        URIMethodResult result = restService.locateRestControllerForURIAndMethod(operation);
        restService.invokeRestController(result, operation);

    }


    @Test
    public void testLocalURIGetExecuted() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/foo/milk/bar/cake123?someQuery=happy&anotherQuery=baby"));
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) ->
                        Assert.assertEquals(
                                "FooBarNormal:/foo/milk/bar/cake123?someQuery=happy&anotherQuery=baby",
                                response
                        )

        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testLocalURIPostExecuted() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/post-mapping"));
        operation.setMethod(HttpMethod.POST);
        operation.setBody("Pretty.Melody");
        operation.setSuccessHandler(
                (String response) -> Assert.assertEquals("postMapping-Pretty.Melody", response)
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testLocalURIPostDTOAndQueryExecuted() throws Exception {

        RestOperation<Object, SampleDTO> operation = new RestOperation<>();
        operation.setApiClass(SampleDTO.class.getName());
        operation.setUri(new URI("/post-mapping/dto/?value=99"));
        operation.setMethod(HttpMethod.POST);
        operation.setBody("My Lovely Melody");
        operation.setSuccessHandler(
                (SampleDTO dto) -> {
                    Assert.assertEquals("My Lovely Melody", dto.getName());
                    Assert.assertEquals(99, dto.getValue());
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testLocalURIHeaderExecuted() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Some-Header","Melody");

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/header-check"));
        operation.setMethod(HttpMethod.GET);
        operation.setHeaders(headers);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("headerCheckSingle-Melody", response);
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testLocalURIHeaderMultipleExecuted() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Some-Header","Happy");
        headers.put("Another-Header","Baby");

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/header-check-multi"));
        operation.setMethod(HttpMethod.GET);
        operation.setHeaders(headers);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("headerCheckMulti-Happy-Baby", response);
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testLocalURIHeaderMultipleNoNameExecuted() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("someHeader","Pretty");
        headers.put("anotherHeader","Melody");

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/header-check-multi-noname"));
        operation.setMethod(HttpMethod.GET);
        operation.setHeaders(headers);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("headerCheckMultiNoName-Pretty-Melody", response);
                }
        );

        restService.restServiceRequest(operation);
    }

}
