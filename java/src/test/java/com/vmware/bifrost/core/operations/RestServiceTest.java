package com.vmware.bifrost.core.operations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.core.model.RestOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import samples.Application;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RunWith(SpringRunner.class)
@RestClientTest(RestService.class)
@ContextConfiguration(classes={Application.class})
public class RestServiceTest {

    @Autowired
    private RestService restService;

    @Autowired
    private MessagebusService bus;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;


    private MockResponseA buildMockResponseA() {
        MockResponseA mockResponseA = new MockResponseA();
        mockResponseA.setName("John Smith");
        mockResponseA.setValue("123 Fancy Street");
        return mockResponseA;
    }

    private MockResponseB buildMockResponseB() {
        MockResponseB mockResponseB = new MockResponseB();
        mockResponseB.setId(UUID.randomUUID());
        mockResponseB.setValue("Melody");
        return mockResponseB;
    }

    private String mapResponseToSting(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }


    private void configureMockRestServer(String uri, String response) {
        this.server.expect(requestTo(uri))
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
                    assertThat(response.getName()).isEqualTo("John Smith");
                    assertThat(response.getValue()).isEqualTo("123 Fancy Street");
                }
        );

        restService.restServiceRequest(operation);

    }

    @Test
    public void testGetWithFullURI() throws Exception {

        configureMockRestServer(
                "http://what-a-lovely.domain/user/1",
                mapResponseToSting(buildMockResponseA())
        );


        RestOperation<Object, MockResponseA> operation = new RestOperation<>();
        operation.setApiClass(MockResponseA.class.getName());
        operation.setUri(new URI("http://what-a-lovely.domain/user/1"));
        operation.setMethod(HttpMethod.GET);
        operation.setSuccessHandler(
                (MockResponseA response) -> {
                    assertThat(response.getName()).isEqualTo("John Smith");
                    assertThat(response.getValue()).isEqualTo("123 Fancy Street");
                }
        );

        restService.restServiceRequest(operation);

    }




}
