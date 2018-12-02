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

/**
 *
 */
public class RestControllerReflection {

    /**
     * Locate RestControllers inside the Spring Context
     * @param context
     * @return
     */
    public static Map<String, Object> locateRestControllers(ConfigurableApplicationContext context) {
        return context.getBeansWithAnnotation(RestController.class);
    }

    /**
     * Extract controller methods for @RequestMapping annotations.
     * @param controller
     * @return
     */
    public static Map<String, Method> extractControllerRequestMappings(Object controller) {
        return extractControllerByAnnotation(controller, RequestMapping.class);
    }

    /**
     * Extract controller methods for @PatchMapping annotations.
     * @param controller
     * @return
     */
    public static Map<String, Method> extractControllerPatchMappings(Object controller) {
        return extractControllerByAnnotation(controller, PatchMapping.class);
    }

    /**
     * Extract controller methods for @GetMapping annotations.
     * @param controller
     * @return
     */
    public static Map<String, Method> extractControllerGetMappings(Object controller) {
        return extractControllerByAnnotation(controller, GetMapping.class);
    }

    /**
     * Extract controller methods for @PostMapping annotations.
     * @param controller
     * @return
     */
    public static Map<String, Method> extractControllerPostMappings(Object controller) {
        return extractControllerByAnnotation(controller, PostMapping.class);
    }

    /**
     * Extract controller methods for @PutMapping annotations.
     * @param controller
     * @return
     */
    public static Map<String, Method> extractControllerPutMappings(Object controller) {
        return extractControllerByAnnotation(controller, PutMapping.class);
    }

    /**
     * Extract controller methods for @DeleteMapping annotations.
     * @param controller
     * @return
     */
    public static Map<String, Method> extractControllerDeleteMappings(Object controller) {
        return extractControllerByAnnotation(controller, DeleteMapping.class);
    }

    /**
     * Extract controller by a specific Annotation type
     * @param controller
     * @return
     */
    public static Map<String, Method> extractControllerByAnnotation(Object controller, Class annotationType) {
        List<Method> rawMethods = Arrays.asList(controller.getClass().getDeclaredMethods());
        Map<String, Method> cleanedMethods = new HashMap<>();

        // extract all annotations available on this controller;
        //Annotation[] controllerAnnotations = controllerClass.getAn


        for (Method method : rawMethods) {

            if (method.getAnnotationsByType(annotationType) != null) {
                cleanedMethods.put(method.getName(), method);
            }
        }

        return cleanedMethods;
    }

    /**
     * Extract a method's args/parameters and the types of those arguments
     * @param method
     * @return
     */
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

    /**
     * Extract an ordered list of the paramter/arg names for a method.
     * @param method
     * @return
     */
    public static List<String> extractMethodParameterList(Method method) {
        Parameter[] params = method.getParameters();
        List<String> paramList = new ArrayList<>();

        for (Parameter param : params) {
            paramList.add(param.getName());
        }
        return paramList;
    }

    /**
     * Extract the method's arguments annotations (if used)
     * @param method
     * @return
     */
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

    /**
     * Extract all annotation object value for a methods args/params annotation.
     * @param method
     * @return
     */
    public static Map<String, Object> extractMethodAnnotationValues(Method method) {
        Parameter[] params = method.getParameters();
        Map<String, Object> paramMap = new HashMap<>();

        for (Parameter param : params) {

            if(param.getAnnotations() != null && param.getAnnotations().length >= 1) {
                paramMap.put(param.getName(),
                        RestControllerReflection.extractMethodAnnotation(param,
                                param.getAnnotations()[0].annotationType())
                );
            } else {
                paramMap.put(param.getName(), null);
            }
        }
        return paramMap;
    }

    /**
     * Extract a specific annotation value from a method.
     * @param param
     * @param annotation
     * @return
     */
    public static Object extractMethodAnnotation(Parameter param, Class annotation) {

        switch (annotation.getName()) {
            case "org.springframework.web.bind.annotation.PathVariable":
                return param.getAnnotation(PathVariable.class);

            case "org.springframework.web.bind.annotation.RequestParam":
                return param.getAnnotation(RequestParam.class);

            case "org.springframework.web.bind.annotation.RequestBody":
                return param.getAnnotation(RequestBody.class);

            case "org.springframework.web.bind.annotation.RequestHeader":
                return param.getAnnotation(RequestHeader.class);

            default:
                return null;

        }

    }

}
