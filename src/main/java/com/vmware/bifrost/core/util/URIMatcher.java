/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

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

    private URIMethodResult checkRequestMappingMethods(URI uri, RequestMethod requestMethod,
                                                              Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            RequestMapping annotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
            if(annotation != null && annotation.path().length > 0) {
                String uriString = annotation.path()[0];
                RequestMethod annotationMethod = annotation.method()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, true,
                        result, method, annotationMethod, uriString);

            } else {
                continue;
            }

            if(result != null) break;
        }
        return result;
    }

    private URIMethodResult checkPatchMappingMethods(URI uri, RequestMethod requestMethod,
                                                            Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            PatchMapping mappingAnnotation =  AnnotationUtils.findAnnotation(method, PatchMapping.class);
            if(mappingAnnotation != null) {
                String uriSring = mappingAnnotation.value()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, uriSring);
            } else {
                continue;
            }
        }
        return result;
    }

    private URIMethodResult checkGetMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                          Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            GetMapping mappingAnnotation =  AnnotationUtils.findAnnotation(method, GetMapping.class);
            if(mappingAnnotation != null) {
                String uriSring = mappingAnnotation.value()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, uriSring);
            } else {
                continue;
            }
        }
        return result;
    }

    private URIMethodResult checkPutMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                          Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            PutMapping mappingAnnotation =  AnnotationUtils.findAnnotation(method, PutMapping.class);
            if(mappingAnnotation != null) {
                String uriSring = mappingAnnotation.value()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, uriSring);
            } else {
                continue;
            }
        }
        return result;
    }

    private URIMethodResult checkPostMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                           Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            PostMapping mappingAnnotation =  AnnotationUtils.findAnnotation(method, PostMapping.class);
            if(mappingAnnotation != null) {
                String uriSring = mappingAnnotation.value()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, uriSring);
            } else {
                continue;
            }
        }
        return result;
    }

    private URIMethodResult checkDeleteMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                             Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            DeleteMapping mappingAnnotation =  AnnotationUtils.findAnnotation(method, DeleteMapping.class);
            if(mappingAnnotation != null) {
                String uriSring = mappingAnnotation.value()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                        method, null, uriSring);
            } else {
                continue;
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
