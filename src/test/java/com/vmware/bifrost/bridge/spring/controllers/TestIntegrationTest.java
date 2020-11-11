package com.vmware.bifrost.bridge.spring.controllers;

/*
 * Copyright(c) VMware Inc. 2017
 */


import org.springframework.boot.test.web.client.TestRestTemplate;

import java.net.URL;

import static org.junit.Assert.assertThat;

//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestIntegrationTest {

  //  @LocalServerPort
    private int port;

    private URL base;

    //@Autowired
    private TestRestTemplate template;

//    @Before
//    public void setUp() throws Exception {
//        this.base = new URL("http://localhost:" + port + "/");
//    }
//
//    @Test
//    public void getHello() throws Exception {
//        ResponseEntity<String> response = template.getForEntity(base.toString(),
//                String.class);
//        assertThat(response.getBody(), equalTo("Greetings from The bifrost!"));
//    }
}
