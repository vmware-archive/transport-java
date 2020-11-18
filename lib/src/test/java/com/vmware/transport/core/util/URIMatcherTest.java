/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.util;

import com.vmware.transport.core.operations.MockRestController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestMethod;

import java.net.URI;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        MockRestController.class,
        DefaultParameterNameDiscoverer.class,
        URIMatcher.class,
        RestControllerReflection.class
})
public class URIMatcherTest {

    @Autowired
    private URIMatcher uriMatcher;

    @Test
    public void testBasicURIMatch() throws Exception {

        URIMethodResult result = uriMatcher.findControllerMatch(new URI("/foo"), RequestMethod.GET);
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getPathItems().size());
        Assert.assertEquals(0, result.getMethodArgs().size());
        Assert.assertEquals(0, result.getMethodAnnotationTypes().size());
        Assert.assertEquals(0, result.getMethodAnnotationValues().size());

    }

    @Test
    public void testComplexURIMatch() throws Exception {

        URIMethodResult result = uriMatcher.findControllerMatch(new URI("/foo/puppy/bar/baby"), RequestMethod.GET);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.getPathItems().size());
        Assert.assertEquals(4, result.getMethodArgs().size());
        Assert.assertEquals(4, result.getMethodAnnotationTypes().size());
        Assert.assertEquals(4, result.getMethodAnnotationValues().size());

    }

    @Test
    public void testComplexURIMatchWithQuery() throws Exception {

        URIMethodResult result = uriMatcher.findControllerMatch(new URI("/foo/puppy/bar/baby?query=something"), RequestMethod.GET);
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.getPathItems().size());
        Assert.assertEquals(4, result.getMethodArgs().size());
        Assert.assertEquals(1, result.getQueryString().size());
        Assert.assertEquals(4, result.getMethodAnnotationTypes().size());
        Assert.assertEquals(4, result.getMethodAnnotationValues().size());

    }


    @Test
    public void testCreatePathMap() throws Exception {

        String url1 = "/manufacturer/{mId}/cars/{carId}/colors/{carColorId}";
        String url2 = "/manufacturer/ford/cars/mustang/colors/123";

        Map<String, Object> pathMap = uriMatcher.createPathItemMap(
                URISplitter.split(url1),
                URISplitter.split(url2),
                null
        );

        Assert.assertEquals(3, pathMap.size());
        Assert.assertEquals("ford", pathMap.get("mId"));
        Assert.assertEquals("mustang", pathMap.get("carId"));
        Assert.assertEquals("123", pathMap.get("carColorId"));

    }

}
