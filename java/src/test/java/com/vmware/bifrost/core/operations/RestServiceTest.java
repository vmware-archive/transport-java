package com.vmware.bifrost.core.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.util.RestControllerInvoker;
import com.vmware.bifrost.core.util.RestControllerReflection;
import com.vmware.bifrost.core.util.URIMatcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.client.ExpectedCount.manyTimes;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
        SecurityConfiguration.class,
        RestService.class,
        MockRestController.class,
        RestControllerInvoker.class,
        DefaultParameterNameDiscoverer.class,
        RestControllerReflection.class,
        URIMatcher.class,
        RestTemplate.class
})
public class RestServiceTest {

    @Autowired
    private RestService restService;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(9999));

    @Autowired
    private ObjectMapper objectMapper;


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

    private String mapResponseToString(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    public void testGet() throws Exception {

        stubFor(get(urlEqualTo("/get-user"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(buildMockResponseA()))));


        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());
        operation.setUri(new URI("http://localhost:9999/get-user"));
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

        stubFor(post(urlEqualTo("/post-update"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(mock))));

        RestOperation<Object, MockResponseB> operation = new RestOperation<>();
        operation.setApiClass(MockResponseB.class.getName());
        operation.setUri(new URI("http://localhost:9999/post-update"));
        operation.setMethod(HttpMethod.POST);
        operation.setBody("anything");
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

        stubFor(patch(urlEqualTo("/user-patch"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(mock))));

        RestOperation<Object, MockResponseB> operation = new RestOperation<>();
        operation.setApiClass(MockResponseB.class.getName());
        operation.setUri(new URI("http://localhost:9999/user-patch"));
        operation.setMethod(HttpMethod.PATCH);
        operation.setBody("anything");
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
        stubFor(put(urlEqualTo("/user-put"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(mock))));

        RestOperation<Object, MockResponseB> operation = new RestOperation<>();
        operation.setApiClass(MockResponseB.class.getName());
        operation.setUri(new URI("http://localhost:9999/user-put"));
        operation.setMethod(HttpMethod.PUT);
        operation.setBody("anything");
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

        stubFor(delete(urlEqualTo("/user-delete"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(buildMockResponseA()))));

        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());
        operation.setUri(new URI("http://localhost:9999/user-delete"));
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
    public void testLocalURIPostDTOAndQueryExecutedWithIntegerQuery() throws Exception {

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
    public void testLocalURIPostDTOAndQueryExecutedWithStringQuery() throws Exception {

        RestOperation<Object, SampleDTO> operation = new RestOperation<>();
        operation.setApiClass(SampleDTO.class.getName());
        operation.setUri(new URI("/post-mapping/dto-string/?value=123"));
        operation.setMethod(HttpMethod.POST);
        operation.setBody("My Lovely Melody");
        operation.setSuccessHandler(
                (SampleDTO dto) -> {
                    Assert.assertEquals("My Lovely Melody", dto.getName());
                    Assert.assertEquals(123, dto.getValue());
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testLocalURIPostDTOAndQueryExecutedWithUUIDQuery() throws Exception {

        RestOperation<Object, SampleDTO> operation = new RestOperation<>();
        operation.setApiClass(SampleDTO.class.getName());
        UUID uuid = UUID.randomUUID();
        operation.setUri(new URI("/post-mapping/dto-uuid/?value=" + uuid.toString()));
        operation.setMethod(HttpMethod.POST);
        operation.setBody("My Lovely Melody");
        operation.setSuccessHandler(
                (SampleDTO dto) -> {
                    Assert.assertEquals("My Lovely Melody", dto.getName());
                    Assert.assertEquals(36, dto.getValue()); // num chars
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testLocalURIHeaderExecuted() throws Exception {

        Map<String, String> headers = new HashMap<>();
        headers.put("Some-Header", "Melody");

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
        headers.put("Some-Header", "Happy");
        headers.put("Another-Header", "Baby");

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
        headers.put("someHeader", "Pretty");
        headers.put("anotherHeader", "Melody");

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

    @Test
    @WithMockUser(value = "someuser")
    public void testSpringSecurityPreAuth() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/secured/preauth"));
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("securedPreAuthUser-success", response);
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testSpringSecurityPreAuthWithoutValidUser() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/secured/preauth"));
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError e) -> {
                    Assert.assertEquals("An Authentication object was not found in the SecurityContext", e.message);
                    Assert.assertEquals(new Integer(500), e.errorCode);

                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    @WithMockUser(value = "someuser", roles = {"SOME_OTHER_ROLE"})
    public void testSpringSecurityPreAuthUserWithUnauthenticatedRoles() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/secured/preauth"));
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError e) -> {
                    Assert.assertEquals("Access is denied", e.message);
                    Assert.assertEquals(new Integer(401), e.errorCode);

                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    @WithMockUser(value = "someuser")
    public void testSpringSecurityPostAuth() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/secured/postauth"));
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("securedPostAuthUser-success", response);
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    @WithMockUser(value = "someuser", roles = {"INVALID_ROLE"})
    public void testSpringSecurityPostAuthInavlidRole() throws Exception {

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("/secured/postauth"));
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError e) -> {
                    Assert.assertEquals("Access is denied", e.message);
                    Assert.assertEquals(new Integer(401), e.errorCode);

                }
        );

        restService.restServiceRequest(operation);
    }


    @Test
    public void testAPIClassInvalidThrowsError() throws Exception {

        stubFor(get(urlEqualTo("/invalid-return-class"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("returnAStringNotSampleDTO")));



        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(SampleDTO.class.getName());
        operation.setUri(new URI("http://localhost:9999/invalid-return-class"));
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError e) -> {
                    Assert.assertEquals(new Integer(500), e.errorCode);

                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testAPIClassMissingThrowsError() throws Exception {

        stubFor(get(urlEqualTo("/missing-api-class"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("somethingFancy")));



        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setUri(new URI("http://localhost:9999/missing-api-class"));
        operation.setMethod(HttpMethod.GET);
        operation.setErrorHandler(
                (RestError e) -> {
                    Assert.assertEquals(new Integer(500), e.errorCode);

                }
        );

        restService.restServiceRequest(operation);
    }


}
