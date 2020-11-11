/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.transport.core.util;


import com.vmware.transport.core.operations.MockRestController;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


import java.lang.reflect.Method;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = {
        MockRestController.class,
        DefaultParameterNameDiscoverer.class,
        RestControllerReflection.class
})
public class RestControllerReflectionTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ParameterNameDiscoverer discoverer;

    @Autowired
    private RestControllerReflection restReflectionUtil;

    @Test
    public void lookupControllers() {

        Map<String, Object> controllers = restReflectionUtil.locateRestControllers();

        Assert.assertEquals(1, controllers.size());
        Assert.assertTrue(controllers.containsKey("MockRestController"));
        Assert.assertEquals(MockRestController.class, controllers.get("MockRestController").getClass());

    }

    @Test
    public void extractControllerMethods() {

        Map<String, Method> methods = restReflectionUtil.extractControllerRequestMappings(
                restReflectionUtil.locateRestControllers().get("MockRestController")
        );

        Assert.assertTrue(methods.size() >= 2);
        Assert.assertTrue(methods.containsKey("simpleGetPath"));
        Assert.assertTrue(methods.containsKey("normalGetPath"));

    }

    @Test
    public void extractMethodParameters() {

        Map<String, Method> methods = restReflectionUtil.extractControllerRequestMappings(
                restReflectionUtil.locateRestControllers().get("MockRestController")
        );

        Map<String, Class> methodParams
                = restReflectionUtil.extractMethodParameters(methods.get("simpleGetPath"));

        Assert.assertEquals(2, methodParams.size());
        Assert.assertTrue(methodParams.containsKey("baz"));
        Assert.assertTrue(methodParams.containsKey("bozQuery"));
        Assert.assertEquals(String.class, methodParams.get("baz"));
        Assert.assertEquals(String.class, methodParams.get("bozQuery"));
    }

    @Test
    public void extractMethodAnnotationsCheckTypes() {

        Map<String, Method> methods = restReflectionUtil.extractControllerRequestMappings(
                restReflectionUtil.locateRestControllers().get("MockRestController")
        );

        Map<String, Class> annotationTypes
                = restReflectionUtil.extractMethodAnnotationTypes(methods.get("simpleGetPath"));

        Assert.assertEquals(2, annotationTypes.size());
        Assert.assertTrue(annotationTypes.containsKey("baz"));
        Assert.assertTrue(annotationTypes.containsKey("bozQuery"));
        Assert.assertEquals(PathVariable.class, annotationTypes.get("baz"));
        Assert.assertEquals(RequestParam.class, annotationTypes.get("bozQuery"));

    }

    @Test
    public void extractMethodAnnotationsCheckValues() {

        Map<String, Method> methods = restReflectionUtil.extractControllerRequestMappings(
                restReflectionUtil.locateRestControllers().get("MockRestController")
        );

        Map<String, Class> annotationTypes
                = restReflectionUtil.extractMethodAnnotationTypes(methods.get("simpleGetPath"));

        Map<String, Object> annotationValues
                = restReflectionUtil.extractMethodAnnotationValues(methods.get("simpleGetPath"));



        Assert.assertEquals(2, annotationTypes.size());
        Assert.assertEquals(PathVariable.class, annotationTypes.get("baz"));
        Assert.assertEquals(RequestParam.class, annotationTypes.get("bozQuery"));
        Assert.assertEquals("boz", ((RequestParam)annotationValues.get("bozQuery")).value());
    }

}
