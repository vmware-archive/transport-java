/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;


import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class RestControllerReflection {

    public static Map<String, Object> locateRestControllers(ConfigurableApplicationContext context) {
        return context.getBeansWithAnnotation(RestController.class);
    }

    public static Map<String, Method> extractControllerRequestMappings(Object controller) {
        List<Method> rawMethods = Arrays.asList(controller.getClass().getDeclaredMethods());
        Map<String, Method> cleanedMethods = new HashMap<>();

        for (Method method : rawMethods) {

            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            if (annotation != null) {
                cleanedMethods.put(method.getName(), method);
            }
        }

        return cleanedMethods;
    }

    public static Map<String, Class> extractMethodParameters(Method method) {
        Parameter[] params = method.getParameters();
        Map<String, Class> paramMap = new HashMap<>();

        int index = 0;
        for(Parameter param: params) {
            paramMap.put(param.getName(), method.getParameterTypes()[index]);
            index++;
        }
        return paramMap;
    }

    public static Map<String, Class> extractMethodAnnotaions(Method method) {
        Parameter[] params = method.getParameters();
        Map<String, Class> paramMap = new HashMap<>();

        for(Parameter param: params) {
            paramMap.put(param.getName(), param.getType());
        }
        return paramMap;
    }

}
