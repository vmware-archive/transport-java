/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.core.operations.MockRestController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        MockRestController.class
})
public class URIMatcherTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    public void testBasicURIMatch() throws Exception {

        URIMethodResult result = URIMatcher.findControllerMatch(context, new URI("/foo"));
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getPathItems().size());
        Assert.assertEquals(0, result.getMethodArgs().size());
        Assert.assertEquals(0, result.getMethodAnnotationTypes().size());
        Assert.assertEquals(0, result.getMethodAnnotationValues().size());

    }

    @Test
    public void testComplexURIMatch() throws Exception {

        URIMethodResult result = URIMatcher.findControllerMatch(context, new URI("/foo/puppy/bar/baby"));
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.getPathItems().size());
        Assert.assertEquals(3, result.getMethodArgs().size());
        Assert.assertEquals(3, result.getMethodAnnotationTypes().size());
        Assert.assertEquals(3, result.getMethodAnnotationValues().size());

    }

    @Test
    public void testComplexURIMatchWithQuery() throws Exception {

        URIMethodResult result = URIMatcher.findControllerMatch(context, new URI("/foo/puppy/bar/baby?query=something"));
        Assert.assertNotNull(result);
        Assert.assertEquals(4, result.getPathItems().size());
        Assert.assertEquals(3, result.getMethodArgs().size());
        Assert.assertEquals(1, result.getQueryString().size());
        Assert.assertEquals(3, result.getMethodAnnotationTypes().size());
        Assert.assertEquals(3, result.getMethodAnnotationValues().size());

    }


    @Test
    public void testCreatePathMap() throws Exception {

        String url1 = "/manufacturer/{mId}/cars/{carId}/colors/{carColorId}";
        String url2 = "/manufacturer/ford/cars/mustang/colors/123";

        Map<String, String> pathMap = URIMatcher.createPathItemMap(
                URISplitter.split(url1),
                URISplitter.split(url2)
        );

        Assert.assertEquals(3, pathMap.size());
        Assert.assertEquals("ford", pathMap.get("mId"));
        Assert.assertEquals("mustang", pathMap.get("carId"));
        Assert.assertEquals("123", pathMap.get("carColorId"));

    }

}
