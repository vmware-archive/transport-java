/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class URIMatcher {

    public static URIMethodResult findControllerMatch(ConfigurableApplicationContext context, URI uri, RequestMethod requestMethod) {

        Map<String, Object> controllers = RestControllerReflection.locateRestControllers(context);

        URIMethodResult result = null;

        for (String key : controllers.keySet()) {

            Object controller = controllers.get(key);
            Map<String, Method> methods = RestControllerReflection.extractControllerRequestMappings(controller);

            for (String methodKey : methods.keySet()) {

                Method method = methods.get(methodKey);
                RequestMapping mappingAnnotation = method.getAnnotation(RequestMapping.class);

                // perform a URI match.
                //if (Arrays.asList(mappingAnnotation.value()).contains(uri.getRawPath())) {

                List<String> requestedPathItems = URISplitter.split(uri);
                List<String> controllerPathItems = URISplitter.split(mappingAnnotation.value()[0]);

                if (comparePaths(controllerPathItems, requestedPathItems)) {

                    // check method matches!
                    if(mappingAnnotation.method()[0].equals(requestMethod)) {

                        result = new URIMethodResult();
                        result.setPathItems(controllerPathItems);
                        result.setMethodArgs(RestControllerReflection.extractMethodParameters(method));
                        result.setMethogArgList(RestControllerReflection.extractMethodParameterList(method));
                        result.setMethodAnnotationTypes(RestControllerReflection.extractMethodAnnotationTypes(method));
                        result.setMethodAnnotationValues(RestControllerReflection.extractMethodAnnotationValues(method));
                        result.setQueryString(URISplitter.extractQueryString(uri));
                        result.setPathItemMap(createPathItemMap(controllerPathItems, requestedPathItems));
                        result.setMethod(method);
                        result.setController(controller);
                    }
                }
            }
        }
        return result;
    }

    public static Map<String, String> createPathItemMap(List<String> controllerPathItems, List<String> requestedPathItems) {

        Map<String, String> map = new HashMap<>();
        if (controllerPathItems.size() == requestedPathItems.size()) {

            for (int x = 0; x < controllerPathItems.size(); x++) {

                String pathItem = controllerPathItems.get(x);

                if (pathItem.startsWith("{") && pathItem.endsWith("}")) {
                    map.put(pathItem.replaceAll("\\{([\\w].+)\\}","$1"), requestedPathItems.get(x));
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
