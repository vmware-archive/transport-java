/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;


import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class RestControllerReflection {

    public static Map<String, Object> locateRestControllers(ConfigurableApplicationContext context) {
        return context.getBeansWithAnnotation(RestController.class);
    }

    public static Map<String, Method> extractControllerRequestMappings(Object controller) {
        return extractControllerByAnnotation(controller, RequestMapping.class);
    }

    public static Map<String, Method> extractControllerPatchMappings(Object controller) {
        return extractControllerByAnnotation(controller, PatchMapping.class);
    }

    public static Map<String, Method> extractControllerGetMappings(Object controller) {
        return extractControllerByAnnotation(controller, GetMapping.class);
    }

    public static Map<String, Method> extractControllerMappings(Object controller) {
        return extractControllerByAnnotation(controller, GetMapping.class);
    }

    public static Map<String, Method> extractControllerByAnnotation(Object controller, Class annotationType) {
        List<Method> rawMethods = Arrays.asList(controller.getClass().getDeclaredMethods());
        Map<String, Method> cleanedMethods = new HashMap<>();

        for (Method method : rawMethods) {
            if (method.getAnnotation(annotationType) != null) {
                cleanedMethods.put(method.getName(), method);
            }
        }

        return cleanedMethods;
    }

    public static Map<String, Class> extractMethodParameters(Method method) {
        Parameter[] params = method.getParameters();
        Map<String, Class> paramMap = new HashMap<>();

        int index = 0;
        for (Parameter param : params) {
            paramMap.put(param.getName(), method.getParameterTypes()[index]);
            index++;
        }
        return paramMap;
    }

    public static List<String> extractMethodParameterList(Method method) {
        Parameter[] params = method.getParameters();
        List<String> paramList = new ArrayList<>();

        for (Parameter param : params) {
            paramList.add(param.getName());
        }
        return paramList;
    }

    public static Map<String, Class> extractMethodAnnotationTypes(Method method) {
        Parameter[] params = method.getParameters();
        Map<String, Class> paramMap = new HashMap<>();

        for (Parameter param : params) {
            if(param.getAnnotations() != null && param.getAnnotations().length >= 1) {
                paramMap.put(param.getName(), param.getAnnotations()[0].annotationType());
            } else {
                paramMap.put(param.getName(), null);
            }
        }
        return paramMap;
    }

    public static Map<String, Object> extractMethodAnnotationValues(Method method) {
        Parameter[] params = method.getParameters();
        Map<String, Object> paramMap = new HashMap<>();

        for (Parameter param : params) {

            if(param.getAnnotations() != null && param.getAnnotations().length >= 1) {
                paramMap.put(param.getName(),
                        RestControllerReflection.extractMethodAnnotation(param, param.getAnnotations()[0].annotationType())
                );
            } else {
                paramMap.put(param.getName(), null);
            }
        }
        return paramMap;
    }


    public static Object extractMethodAnnotation(Parameter param, Class annotation) {

        switch (annotation.getName()) {
            case "org.springframework.web.bind.annotation.PathVariable":
                return param.getAnnotation(PathVariable.class);

            case "org.springframework.web.bind.annotation.RequestParam":
                return param.getAnnotation(RequestParam.class);

            case "org.springframework.web.bind.annotation.RequestBody":
                return param.getAnnotation(RequestBody.class);

            default:
                return null;

        }

    }

}
