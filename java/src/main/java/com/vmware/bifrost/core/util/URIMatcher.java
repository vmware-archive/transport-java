/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class URIMatcher {

    public static URIMethodResult findControllerMatch(ConfigurableApplicationContext context, URI uri, RequestMethod requestMethod) {

        Map<String, Object> controllers = RestControllerReflection.locateRestControllers(context);

        URIMethodResult result = null;

        for (String key : controllers.keySet()) {

            Object controller = controllers.get(key);
            Map<String, Method> methods;


            methods = RestControllerReflection.extractControllerRequestMappings(controller);
            result = checkRequestMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            methods = RestControllerReflection.extractControllerPatchMappings(controller);
            result = checkPatchMappingMethods(uri, requestMethod, controller, methods);
        }
        return result;
    }

    private static URIMethodResult checkRequestMappingMethods(URI uri, RequestMethod requestMethod, Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);

            // check for RequestMapping instances;

            RequestMapping mappingAnnotation = method.getAnnotation(RequestMapping.class);
            String uriSring = mappingAnnotation.value()[0];
            RequestMethod annotationMethod = mappingAnnotation.method()[0];

            result = checkForControllerMatch(uri, requestMethod, controller, true, result, method, annotationMethod, uriSring);
        }
        return result;
    }

    private static URIMethodResult checkPatchMappingMethods(URI uri, RequestMethod requestMethod, Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);

            // check for RequestMapping instances;

            PatchMapping mappingAnnotation = method.getAnnotation(PatchMapping.class);
            String uriSring = mappingAnnotation.value()[0];

            result = checkForControllerMatch(uri, requestMethod, controller, false, result, method, null, uriSring);
        }
        return result;
    }


    private static URIMethodResult checkForControllerMatch(URI uri, RequestMethod requestMethod, Object controller, boolean checkMethod, URIMethodResult result, Method method, RequestMethod annotationMethod, String uriSring) {
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

    private static URIMethodResult buildURIMethodResult(URI uri, Object controller, Method method, List<String> requestedPathItems, List<String> controllerPathItems) {
        URIMethodResult result;
        result = new URIMethodResult();
        result.setPathItems(controllerPathItems);
        result.setMethodArgs(RestControllerReflection.extractMethodParameters(method));
        result.setMethogArgList(RestControllerReflection.extractMethodParameterList(method));
        result.setMethodAnnotationTypes(RestControllerReflection.extractMethodAnnotationTypes(method));
        result.setMethodAnnotationValues(RestControllerReflection.extractMethodAnnotationValues(method));
        result.setQueryString(URISplitter.extractQueryString(uri));
        result.setPathItemMap(createPathItemMap(controllerPathItems, requestedPathItems, RestControllerReflection.extractMethodParameters(method)));
        result.setMethod(method);
        result.setController(controller);
        return result;
    }

    public static Map<String, Object> createPathItemMap(List<String> controllerPathItems, List<String> requestedPathItems, Map<String, Class> methodArgs) {

        Map<String, Object> map = new HashMap<>();
        if (controllerPathItems.size() == requestedPathItems.size()) {

            for (int x = 0; x < controllerPathItems.size(); x++) {

                String pathItem = controllerPathItems.get(x);

                if (pathItem.startsWith("{") && pathItem.endsWith("}")) {

                    String argName = pathItem.replaceAll("\\{([\\w].+)\\}", "$1");

                    try {

                        if(methodArgs != null) {

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

    public static boolean comparePaths(List<String> controllerPathItems, List<String> requestedPathItems) {

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
