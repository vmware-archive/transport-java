/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
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
public class URIMatcher {

    /**
     * Generate a URIMethodResult if a local URI for a specific request method can be located.
     * @param context
     * @param uri
     * @param requestMethod
     * @return
     */
    public static URIMethodResult findControllerMatch(ConfigurableApplicationContext context, URI uri, RequestMethod requestMethod) {

        Map<String, Object> controllers = RestControllerReflection.locateRestControllers(context);

        URIMethodResult result = null;

        for (String key : controllers.keySet()) {

            Object controller = controllers.get(key);
            Map<String, Method> methods;

            // RequestMapping check
            methods = RestControllerReflection.extractControllerRequestMappings(controller);
            result = checkRequestMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // PatchMapping check
            methods = RestControllerReflection.extractControllerPatchMappings(controller);
            result = checkPatchMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // GetMapping Check
            methods = RestControllerReflection.extractControllerGetMappings(controller);
            result = checkGetMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // PutMapping Check
            methods = RestControllerReflection.extractControllerPutMappings(controller);
            result = checkPutMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // PostMapping Check
            methods = RestControllerReflection.extractControllerPostMappings(controller);
            result = checkPostMappingMethods(uri, requestMethod, controller, methods);

            if (result != null) break;

            // DeleteMapping Check
            methods = RestControllerReflection.extractControllerDeleteMappings(controller);
            result = checkDeleteMappingMethods(uri, requestMethod, controller, methods);

        }
        return result;
    }

    private static URIMethodResult checkRequestMappingMethods(URI uri, RequestMethod requestMethod,
                                                              Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            RequestMapping annotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
            if(annotation != null) {
                String uriSring = annotation.path()[0];
                RequestMethod annotationMethod = annotation.method()[0];

                result = checkForControllerMatch(uri, requestMethod, controller, true,
                        result, method, annotationMethod, uriSring);

            } else {
                continue;
            }

            if(result != null) break;
        }
        return result;
    }

    private static URIMethodResult checkPatchMappingMethods(URI uri, RequestMethod requestMethod,
                                                            Object controller, Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            PatchMapping mappingAnnotation = method.getAnnotation(PatchMapping.class);
            String uriSring = mappingAnnotation.value()[0];

            result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                    method, null, uriSring);
        }
        return result;
    }

    private static URIMethodResult checkGetMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                          Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            GetMapping mappingAnnotation = method.getAnnotation(GetMapping.class);
            String uriSring = mappingAnnotation.value()[0];

            result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                    method, null, uriSring);
        }
        return result;
    }

    private static URIMethodResult checkPutMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                          Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            PutMapping mappingAnnotation = method.getAnnotation(PutMapping.class);
            String uriSring = mappingAnnotation.value()[0];

            result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                    method, null, uriSring);
        }
        return result;
    }

    private static URIMethodResult checkPostMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                           Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            PostMapping mappingAnnotation = method.getAnnotation(PostMapping.class);
            String uriSring = mappingAnnotation.value()[0];

            result = checkForControllerMatch(uri, requestMethod, controller, false, result,
                    method, null, uriSring);
        }
        return result;
    }

    private static URIMethodResult checkDeleteMappingMethods(URI uri, RequestMethod requestMethod, Object controller,
                                                             Map<String, Method> methods) {
        URIMethodResult result = null;
        for (String methodKey : methods.keySet()) {

            Method method = methods.get(methodKey);
            DeleteMapping mappingAnnotation = method.getAnnotation(DeleteMapping.class);
            String uriSring = mappingAnnotation.value()[0];

            result = checkForControllerMatch(uri, requestMethod, controller, false, result, method,
                    null, uriSring);
        }
        return result;
    }


    private static URIMethodResult checkForControllerMatch(URI uri, RequestMethod requestMethod, Object controller,
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

    private static URIMethodResult buildURIMethodResult(URI uri, Object controller, Method method, List<String>
            requestedPathItems, List<String> controllerPathItems) {
        URIMethodResult result;
        result = new URIMethodResult();
        result.setPathItems(controllerPathItems);
        result.setMethodArgs(RestControllerReflection.extractMethodParameters(method));
        result.setMethogArgList(RestControllerReflection.extractMethodParameterList(method));
        result.setMethodAnnotationTypes(RestControllerReflection.extractMethodAnnotationTypes(method));
        result.setMethodAnnotationValues(RestControllerReflection.extractMethodAnnotationValues(method));
        result.setQueryString(URISplitter.extractQueryParams(uri));
        result.setPathItemMap(
                createPathItemMap(
                        controllerPathItems,
                        requestedPathItems,
                        RestControllerReflection.extractMethodParameters(method)
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
    public static Map<String, Object> createPathItemMap(List<String> controllerPathItems, List<String>
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
