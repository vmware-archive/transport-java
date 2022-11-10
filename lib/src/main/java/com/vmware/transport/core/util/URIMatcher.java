/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.util;

import org.springframework.aop.framework.Advised;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Performs controller matching to incoming REST Requests.
 */
@Component
public class URIMatcher {

    private final RestControllerReflection reflectionUtil;

    URIMatcher(RestControllerReflection reflectionUtil) {
        this.reflectionUtil = reflectionUtil;
    }

    /**
     * Generate a URIMethodResult if a local URI for a specific request method can be located.
     * @param uri
     * @param requestMethod
     * @return
     */
    public URIMethodResult findControllerMatch(
            URI uri,
            RequestMethod requestMethod) throws Exception {

        Map<String, Object> controllers = reflectionUtil.locateRestControllers();

        URIMethodResult result = null;

        for (String key : controllers.keySet()) {

            Object controllerBean  = controllers.get(key);
            Object controller;

            try {
                Advised advised = (Advised) controllerBean;
                controller = advised.getTargetSource().getTarget();


            } catch (ClassCastException exp) {
                controller = controllerBean;
            }

            Map<String, Method> methods;

            // RequestMapping check
            methods = reflectionUtil.extractControllerRequestMappings(controller);
            result = checkRequestMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // PostMapping Check
            methods = reflectionUtil.extractControllerPostMappings(controller);
            result = checkPostMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // PatchMapping check
            methods = reflectionUtil.extractControllerPatchMappings(controller);
            result = checkPatchMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // GetMapping Check
            methods = reflectionUtil.extractControllerGetMappings(controller);
            result = checkGetMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // PutMapping Check
            methods = reflectionUtil.extractControllerPutMappings(controller);
            result = checkPutMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // DeleteMapping Check
            methods = reflectionUtil.extractControllerDeleteMappings(controller);
            result = checkDeleteMappingMethods(uri, requestMethod, controller, methods);

        }
        return result;
    }

    /**
     * Looks up the RequestMapping annotation on the controller class and populates a new StringBuilder object with
     * the URI paths segments it finds in the annotation, then returns the StringBuilder object.
     *
     * @param controller controller object
     * @return a new StringBuilder object filled with URI paths
     */
    private StringBuilder getControllerAnnotationValues(Object controller) {
        StringBuilder urlBuilder = new StringBuilder();
        RequestMapping controllerReqMapping = AnnotationUtils.findAnnotation(controller.getClass(), RequestMapping.class);

        if (controllerReqMapping != null) {
            for (String segment : controllerReqMapping.value()) {
                urlBuilder.append(segment);
            }
        }
        return urlBuilder;
    }

    private URIMethodResult checkRequestMappingMethods(URI uri, RequestMethod requestMethod,
                                                              Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {
            Method method = methods.get(methodKey);
            StringBuilder urlBuilder = getControllerAnnotationValues(controller);
            Consumer<String> appendToUrlBuilder = urlBuilder::append;
            RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);

            if (requestMapping != null && requestMapping.path().length > 0) {
                Arrays.stream(requestMapping.path()).forEach(appendToUrlBuilder);
                RequestMethod annotationMethod = requestMapping.method()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, true,
                        result, method, annotationMethod, urlBuilder.toString());
                if (result != null) {
                    break;
                }
            }
        }
        return result;
    }

    private URIMethodResult checkPatchMappingMethods(URI uri, RequestMethod requestMethod,
                                                            Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {
            Method method = methods.get(methodKey);
            StringBuilder urlBuilder = getControllerAnnotationValues(controller);
            Consumer<String> appendToUrlBuilder = urlBuilder::append;
            PatchMapping patchMapping = AnnotationUtils.findAnnotation(method, PatchMapping.class);

            if (patchMapping != null && patchMapping.value().length > 0) {
                Arrays.stream(patchMapping.value()).forEach(appendToUrlBuilder);
            }
            if (urlBuilder.length() > 0) {
                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, urlBuilder.toString());
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    private URIMethodResult checkGetMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                          Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {
            Method method = methods.get(methodKey);
            StringBuilder urlBuilder = getControllerAnnotationValues(controller);
            Consumer<String> appendToUrlBuilder = urlBuilder::append;
            GetMapping getMapping = AnnotationUtils.findAnnotation(method, GetMapping.class);

            if (getMapping != null && getMapping.value().length > 0) {
                Arrays.stream(getMapping.value()).forEach(appendToUrlBuilder);
            }
            if (urlBuilder.length() > 0) {
                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, urlBuilder.toString());
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    private URIMethodResult checkPutMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                          Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {
            Method method = methods.get(methodKey);
            StringBuilder urlBuilder = getControllerAnnotationValues(controller);
            Consumer<String> appendToUrlBuilder = urlBuilder::append;
            PutMapping putMapping  = AnnotationUtils.findAnnotation(method, PutMapping.class);

            if (putMapping != null && putMapping.value().length > 0) {
                Arrays.stream(putMapping.value()).forEach(appendToUrlBuilder);
            }
            if (urlBuilder.length() > 0) {
                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, urlBuilder.toString());
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }
    private URIMethodResult checkPostMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                           Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {
            Method method = methods.get(methodKey);
            StringBuilder urlBuilder = getControllerAnnotationValues(controller);
            Consumer<String> appendToUrlBuilder = urlBuilder::append;
            PostMapping postMapping = AnnotationUtils.findAnnotation(method, PostMapping.class);

            if (postMapping != null && postMapping.value().length > 0) {
                Arrays.stream(postMapping.value()).forEach(appendToUrlBuilder);
            }
            if (urlBuilder.length() > 0) {
                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, urlBuilder.toString());
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }

    private URIMethodResult checkDeleteMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                             Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {
            Method method = methods.get(methodKey);
            StringBuilder urlBuilder = getControllerAnnotationValues(controller);
            Consumer<String> appendToUrlBuilder = urlBuilder::append;
            DeleteMapping deleteMapping = AnnotationUtils.findAnnotation(method, DeleteMapping.class);

            if (deleteMapping != null && deleteMapping.value().length > 0) {
                Arrays.stream(deleteMapping.value()).forEach(appendToUrlBuilder);
            }
            if (urlBuilder.length() > 0) {
                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, urlBuilder.toString());
            }
            if (result != null) {
                break;
            }
        }
        return result;
    }


    private URIMethodResult checkForControllerMatch(URI uri, RequestMethod requestMethod, Object controller,
                                                           boolean checkMethod, URIMethodResult result, Method method,
                                                           RequestMethod annotationMethod, String uriSring) {
        List<String> requestedPathItems = URISplitter.split(uri);
        List<String> controllerPathItems = URISplitter.split(uriSring);

        if (comparePaths(controllerPathItems, requestedPathItems)) {

            // check method matches!
            if (checkMethod) {
                if (annotationMethod.equals(requestMethod))
                    result = buildURIMethodResult(uri, controller, method, requestedPathItems, controllerPathItems);

            } else {
                result = buildURIMethodResult(uri, controller, method, requestedPathItems, controllerPathItems);
            }
        }
        return result;
    }

    private URIMethodResult buildURIMethodResult(
            URI uri, Object controller, Method method, List<String>
            requestedPathItems, List<String> controllerPathItems) {
        URIMethodResult result;
        result = new URIMethodResult();
        result.setPathItems(controllerPathItems);
        result.setMethodArgs(reflectionUtil.extractMethodParameters(method));
        result.setMethogArgList(reflectionUtil.extractMethodParameterList(method));
        result.setMethodAnnotationTypes(reflectionUtil.extractMethodAnnotationTypes(method));
        result.setMethodAnnotationValues(reflectionUtil.extractMethodAnnotationValues(method));
        result.setQueryString(URISplitter.extractQueryParams(uri, result.getMethodArgs()));
        result.setPathItemMap(
                createPathItemMap(
                        controllerPathItems,
                        requestedPathItems,
                        reflectionUtil.extractMethodParameters(method)
                )
        );
        result.setMethod(method);
        result.setController(controller);
        return result;
    }

    /**
     * Create a Path Item Map (maps path variable names, to the actual values submitted as a part of the URI);
     * @param controllerPathItems
     * @param requestedPathItems
     * @param methodArgs
     * @return
     */
    public Map<String, Object> createPathItemMap(List<String> controllerPathItems, List<String>
            requestedPathItems, Map<String, Class> methodArgs) {

        Map<String, Object> map = new HashMap<>();
        if (controllerPathItems.size() == requestedPathItems.size()) {

            for (int x = 0; x < controllerPathItems.size(); x++) {

                String pathItem = controllerPathItems.get(x);

                if (pathItem.startsWith("{") && pathItem.endsWith("}")) {

                    String argName = pathItem.replaceAll("\\{([\\w].+)\\}", "$1");

                    try {

                        if (methodArgs != null) {

                            Class methodArgClass = methodArgs.get(argName);
                            if (methodArgClass.equals(UUID.class)) {

                                // check if the path variable is a UUID or not.
                                UUID uuid = UUID.fromString(requestedPathItems.get(x));
                                map.put(argName, uuid);
                            } else {
                                map.put(argName, requestedPathItems.get(x));
                            }
                        } else {
                            map.put(argName, requestedPathItems.get(x));
                        }

                    } catch (IllegalArgumentException e) {

                        // treat as string.
                        map.put(argName, requestedPathItems.get(x));
                    }
                }
            }
        }
        return map;

    }

    /**
     * Compare paths to see if they match, skipping over variables as they are wildcards.
     * @param controllerPathItems
     * @param requestedPathItems
     * @return
     */
    public boolean comparePaths(List<String> controllerPathItems, List<String> requestedPathItems) {

        boolean match = false;

        if (controllerPathItems.size() == requestedPathItems.size()) {

            int index = 0;
            for (String pathItem : controllerPathItems) {
                if (pathItem.startsWith("{") && pathItem.endsWith("}")) {
                    match = true;
                    index++;
                    continue;
                }
                if (pathItem.equals(requestedPathItems.get(index))) {
                    match = true;
                } else {
                    match = false;
                    break;
                }
                index++;
            }
        }
        return match;

    }
}
