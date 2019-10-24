package com.vmware.bifrost.core.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.EventBusImpl;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.bus.store.BusStoreApi;
import com.vmware.bifrost.bus.store.StoreManager;
import com.vmware.bifrost.bus.model.MessageObjectHandlerConfig;
import com.vmware.bifrost.bus.store.model.BusStore;
import com.vmware.bifrost.core.AbstractBase;
import com.vmware.bifrost.core.CoreChannels;
import com.vmware.bifrost.core.CoreStoreKeys;
import com.vmware.bifrost.core.CoreStores;
import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;
import com.vmware.bifrost.core.model.RestServiceRequest;
import com.vmware.bifrost.core.util.RestControllerInvoker;
import com.vmware.bifrost.core.util.RestControllerReflection;
import com.vmware.bifrost.core.util.ServiceMethodLookupUtil;
import com.vmware.bifrost.core.util.URIMatcher;
import org.hamcrest.Matchers;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;


import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

// create a mock consumer of the rest service to test it operates over the bus.

class FakeRestUser extends AbstractBase {

    FakeRestUser(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void initialize() {
        // who cares.
    }

    public void doTheRequest(RestOperation operation) {
        this.restServiceRequest(operation);
    }

    public void overrideHostandPost() {
        // set correct port.
        this.setGlobalRestServiceHostOptions("localhost", "9999");
    }
}


@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        SecurityConfiguration.class,
        RestService.class,
        MockRestController.class,
        RestControllerInvoker.class,
        DefaultParameterNameDiscoverer.class,
        RestControllerReflection.class,
        URIMatcher.class,
        RestTemplate.class,
        EventBusImpl.class,
        StoreManager.class,
        ServiceMethodLookupUtil.class
})
public class RestServiceTest {

    @Autowired
    private RestService restService;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(9999));

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventBus bus;

    @Autowired
    private BusStoreApi storeManager;

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
    public void testModifyBaseHost() throws Exception {

        BusStore<String, String> baseHostStore = this.bus.getStoreManager().getStore(CoreStores.RestServiceHostConfig);

        // override the base host to be localhost, so the test passes, otherwise it will fail because of the bad host.
        baseHostStore.put(CoreStoreKeys.RestServiceBaseHost, "localhost", null);

        stubFor(get(urlEqualTo("/something"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(buildMockResponseA()))));


        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());

        // define a bad host.
        operation.setUri(new URI("http://guaranteed-to-fail:9999/something"));
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
    public void testModifyBasePort() throws Exception {

        BusStore<String, String> baseHostStore = this.bus.getStoreManager().getStore(CoreStores.RestServiceHostConfig);

        // override the port to be the correct port, so the test passes, otherwise it will fail.
        baseHostStore.put(CoreStoreKeys.RestServiceBasePort, "9999", null);

        stubFor(get(urlEqualTo("/something"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(buildMockResponseA()))));


        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());

        // define a bad port
        operation.setUri(new URI("http://localhost:91231231412/something"));
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
    public void testModifyBasePortAndHostViaAbstractBase() throws Exception {

        FakeRestUser fu = new FakeRestUser(this.bus);
        fu.overrideHostandPost();

        stubFor(get(urlEqualTo("/something"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(buildMockResponseA()))));


        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());

        // define a bad port
        operation.setUri(new URI("http://going-to-fail:8899/something"));
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
    public void testBusRequestHandling() throws Exception {

        stubFor(get(urlEqualTo("/bus-test"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody(mapResponseToString(buildMockResponseB()))));

        FakeRestUser fakey = new FakeRestUser(bus);
        Observable<Message> chan = this.bus.getApi().getChannel(CoreChannels.RestService, "rest-test");
        TestObserver<Message> observer = chan.test();


        RestOperation<Object, MockResponseB> operation = new RestOperation<>();
        operation.setId(UUID.randomUUID());
        operation.setApiClass(MockResponseB.class.getName());
        operation.setUri(new URI("http://localhost:9999/bus-test"));
        operation.setMethod(HttpMethod.GET);

        operation.setSuccessHandler(
                (MockResponseB response) -> {
                    // close, so the test can complete.
                    bus.getApi().close(CoreChannels.RestService, "rest-test");
                }
        );

        fakey.doTheRequest(operation);

        // wait for 10ms or close.
        observer.await(10, TimeUnit.MILLISECONDS);
        assertThat(observer.valueCount()).isEqualTo(2);

        Message<Response<MockResponseB>> msg = observer.values().get(0); // LIFO
        MockResponseB mresp = msg.getPayload().getPayload();
        assertThat(mresp.getValue()).isEqualTo("Pizza");

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
    public void testRemoteURIHeaderExecuted() throws Exception {

        stubFor(get(urlEqualTo("/remote-headers"))
                .withHeader("Some-Header", containing("Melody"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("headersWork!")));


        Map<String, String> headers = new HashMap<>();
        headers.put("Some-Header", "Melody");

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setApiClass(String.class.getName());
        operation.setUri(new URI("http://localhost:9999/remote-headers"));
        operation.setMethod(HttpMethod.GET);
        operation.setHeaders(headers);
        operation.setSuccessHandler(
                (String response) -> {
                    Assert.assertEquals("headersWork!", response);
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

    @Test
    public void testOperationInvalidThrowsError() throws Exception {

        stubFor(get(urlEqualTo("/missing-api-class"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("somethingFancy")));

        RestOperation<Object, String> operation = new RestOperation<>();
        operation.setUri(new URI("http://localhost:9999/bad_operation"));
        operation.setErrorHandler(
                (RestError e) -> {
                    Assert.assertEquals(new Integer(500), e.errorCode);
                }
        );

        restService.restServiceRequest(operation);
    }

    @Test
    public void testOperatesOverBus() throws Exception {

        stubFor(get(urlEqualTo("/bus-uri"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("bus-request-success")));

        UUID id = UUID.randomUUID();
        RestServiceRequest req = new RestServiceRequest();
        req.setApiClass(String.class.getName());
        req.setMethod(HttpMethod.GET);
        req.setUri(new URI("http://localhost:9999/bus-uri"));
        req.setSentFrom(this.getClass().getSimpleName());

        // we need to wrap a rest service request into a request.
        Request<RestServiceRequest> request = new Request<>();
        request.setId(id);
        request.setChannel(CoreChannels.RestService);
        request.setPayload(req);

        this.bus.requestOnceWithId(
                id,
                CoreChannels.RestService,
                request,
                (Message message) -> {
                    Response resp = (Response) message.getPayload();
                    Assert.assertEquals("\"bus-request-success\"", resp.getPayload().toString());
                },
                (Message message) -> {
                    Assert.fail();
                }
        );
    }

    @Test
    public void testHandlesErrorsOverBus() throws Exception {

        stubFor(get(urlEqualTo("/bus-uri-error"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("there-be-monsters-down-here!")));

        UUID id = UUID.randomUUID();
        RestServiceRequest req = new RestServiceRequest();
        req.setApiClass(String.class.getName());
        req.setMethod(HttpMethod.GET);
        req.setUri(new URI("http://localhost:9999/bus-uri-error"));
        req.setSentFrom(this.getClass().getSimpleName());

        // we need to wrap a rest service request into a request.
        Request<RestServiceRequest> request = new Request<>();
        request.setId(id);
        request.setChannel(CoreChannels.RestService);
        request.setPayload(req);

        //req.setId(id);
        this.bus.requestOnceWithId(
                id,
                CoreChannels.RestService,
                request,
                (Message message) -> {
                    Assert.fail();
                },
                (Message message) -> {
                    Response<RestError> resp = (Response<RestError>) message.getPayload();
                    RestError error = resp.getPayload();
                    Assert.assertEquals(new Integer(500), error.errorCode);
                    Assert.assertEquals(
                            "REST Client Error, unable to complete request: http://localhost:9999/bus-uri-error", error.message);
                }
        );
    }

    @Test
    public void testGenericExceptionCaughtAndHandled() throws Exception {

        stubFor(get(urlEqualTo("/throw-exception"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.BAD_REQUEST.value())));

        UUID id = UUID.randomUUID();
        RestServiceRequest req = new RestServiceRequest();

        // this should cause restServiceRequest to throw a ClassNotFoundException
        req.setApiClass("com.fake.ClassDoesNotExist");
        req.setMethod(HttpMethod.GET);
        req.setUri(new URI("http://localhost:9999/throw-exception"));
        req.setSentFrom(this.getClass().getSimpleName());
        //req.setId(id);
        // we need to wrap a rest service request into a request.
        Request<RestServiceRequest> request = new Request<>();
        request.setId(id);
        request.setChannel(CoreChannels.RestService);
        request.setPayload(req);

        this.bus.requestOnceWithId(
                id,
                CoreChannels.RestService,
                request,
                (Message message) -> {
                    Assert.fail();
                },
                (Message message) -> {
                    Response<RestError> resp = (Response) message.getPayload();
                    RestError error = resp.getPayload();
                    Assert.assertEquals(new Integer(500), error.errorCode);
                    Assert.assertEquals(
                            "Class Not Found Exception thrown for: http://localhost:9999/throw-exception", error.message);
                }
        );
    }

    @Test
    public void testHeaderMerge() throws Exception {
        stubFor(get(urlEqualTo("/header"))
                .withHeader("X-Custom-Header", containing("Custom Value"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                        .withBody("success")));

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom-Header", "Custom Value");

        UUID id = UUID.randomUUID();
        RestServiceRequest req = new RestServiceRequest();
        req.setApiClass(String.class.getName());
        req.setMethod(HttpMethod.GET);
        req.setUri(new URI("http://localhost:9999/header"));
        req.setSentFrom(this.getClass().getSimpleName());

        Request<RestServiceRequest> request = new Request<>();
        request.setId(id);
        request.setChannel(CoreChannels.RestService);
        request.setHeaders(headers);
        request.setPayload(req);

        this.bus.requestOnceWithId(
                id,
                CoreChannels.RestService,
                request,
                (Message message) -> {},
                (Message message) -> {
                    Assert.fail();
                });
    }

    @Test
    public void testBadRequest() throws Exception {
        stubFor(get(urlEqualTo("/bifrost"))
                .willReturn(aResponse().withStatus(org.apache.http.HttpStatus.SC_OK)));

        UUID id = UUID.randomUUID();
        RestServiceRequest restRequest = new RestServiceRequest();
        restRequest.setApiClass("BadApiClass");
        restRequest.setMethod(HttpMethod.POST);
        restRequest.setSentFrom("no-body");
        restRequest.setUri(new URI("http://localhost:9999/bifrost"));

        Request<String> request = new Request<>();
        request.setId(id);
        request.setPayload("I am a bad request!");
        request.setChannel(CoreChannels.RestService);

        this.bus.requestOnceWithId(
                id,
                CoreChannels.RestService,
                request,
                (Message message) -> {
                    System.out.println(message.getPayloadClass().getName());
                    Response response = (Response) message.getPayload();
                    Assert.assertTrue(response.isError());
                    Assert.assertEquals(500, response.getErrorCode());
                    Assert.assertThat(response.getErrorMessage(), Matchers.containsString("Exception thrown"));
                },
                (Message message) -> {});
    }
}
