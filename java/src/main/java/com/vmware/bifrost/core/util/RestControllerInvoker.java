/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;
import com.vmware.bifrost.core.error.RestError;
import com.vmware.bifrost.core.model.RestOperation;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Component
public class RestControllerInvoker {

    RestControllerInvoker() {

    }

    /**
     * Check method can be called and invoke accordingly.
     * @param methodResult
     * @param operation
     * @throws RuntimeException
     */
    public void invokeMethod(URIMethodResult methodResult, RestOperation operation) throws RuntimeException {

        Object[] formulatedMethodArgs = new Object[methodResult.getMethogArgList().size()];
        RestError error = null;

        if (methodResult.getMethodArgs().size() >= 1) {

            Map<String, Object> pathItemMap = methodResult.getPathItemMap();
            Map<String, Class> methodArgs = methodResult.getMethodArgs();
            Map<String, Class> methodAnnotationTypes = methodResult.getMethodAnnotationTypes();


            // check if path items and method args match
            if(pathItemMap.size() >= 1) {

                if (doPathItemsAndMethodArgsMatch(pathItemMap, methodArgs, methodAnnotationTypes)) {

                    // iterate through all our data and construct the correct signature to call.
                    error = processMethod(methodResult, pathItemMap, formulatedMethodArgs, operation);

                    // send error instead of invoking method, bypass completely.
                    if (error != null) {
                        operation.getErrorHandler().accept(error);
                        return;
                    }

                    callControllerMethod(methodResult, operation, formulatedMethodArgs);


                } else {

                    operation.getSuccessHandler().accept("sad munkey :(");
                }

            } else {

                error = processMethod(methodResult, pathItemMap, formulatedMethodArgs, operation);

                if (error != null) {
                    operation.getErrorHandler().accept(error);
                    return;
                }

                callControllerMethod(methodResult, operation, formulatedMethodArgs);
            }

        } else {

            callControllerMethod(methodResult, operation, null);
        }
    }

    /**
     * Validate path and method arguements align
     * @param pathItemMap
     * @param methodArgs
     * @param methodAnnotations
     * @return
     */
    public boolean doPathItemsAndMethodArgsMatch(
            Map<String, Object> pathItemMap,
            Map<String, Class> methodArgs,
            Map<String, Class> methodAnnotations
    ) {

        boolean match = false;

        for (String pathItem : pathItemMap.keySet()) {

            String className = methodAnnotations.get(pathItem).getName();
            switch (className) {
                case "org.springframework.web.bind.annotation.PathVariable":
                    if (methodArgs.get(pathItem) != null) {
                        match = true;
                        continue;
                    } else {
                        match = false;
                        break;
                    }
                default:
                    break;
            }
        }
        return match;
    }

    private void callControllerMethod(URIMethodResult methodResult, RestOperation operation, Object[] formulatedMethodArgs) {
        Method method = methodResult.getMethod();
        Object controller = methodResult.getController();

        try {

            if(formulatedMethodArgs!= null) {
                operation.getSuccessHandler().accept(
                        method.invoke(controller, formulatedMethodArgs)
                );
            } else {
                operation.getSuccessHandler().accept(
                        method.invoke(controller)
                );
            }

        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private RestError processMethod(URIMethodResult methodResult, Map<String, Object> pathItemMap,
                                    Object[] formulatedMethodArgs, RestOperation operation) {

        RestError error = null;
        int index = 0;
        for (String paramName : methodResult.getMethogArgList()) {

            // check we have an annotation.
            if (methodResult.getMethodAnnotationTypes().get(paramName) != null) {

                // check PathVariable argument.
                if (methodResult.getMethodAnnotationTypes().get(paramName).equals(PathVariable.class)) {
                    formulatedMethodArgs[index] = pathItemMap.get(paramName);
                }

                // check RequestBody argument.
                if (methodResult.getMethodAnnotationTypes().get(paramName).equals(RequestBody.class)) {
                    formulatedMethodArgs[index] = operation.getBody();
                }

                if (methodResult.getMethodAnnotationTypes().get(paramName).equals(RequestParam.class)) {

                    RequestParam requestParam = (RequestParam) methodResult.getMethodAnnotationValues().get(paramName);

                    if (requestParam != null && !requestParam.value().isEmpty()) {
                        if (requestParam.required()) {
                            if (methodResult.getQueryString() == null) {

                                error = getRestError(
                                        "Method requires request parameters, however none have been supplied.",
                                        "REST Error: Missing Request Parameters"
                                );

                            } else {

                                if (methodResult.getQueryString().get(requestParam.value()) == null) {

                                    error = getRestError(
                                            "Method requires request param '" + requestParam.value()
                                                    + "', This maps to method argument '" + paramName
                                                    + "', but wasn't supplied with URI properties.",
                                            "REST Error: Invalid Request Parameters");

                                } else {

                                    // cannot be null.
                                    formulatedMethodArgs[index] =
                                            methodResult.getQueryString().get(requestParam.value());

                                }
                            }
                        } else {

                            if (checkForNullQueryString(methodResult)) {

                                // can be null
                                formulatedMethodArgs[index] = null;
                            } else {

                                if (methodResult.getQueryString().get(requestParam.value()) == null) {

                                    // can be null
                                    formulatedMethodArgs[index] = null;

                                } else {

                                    formulatedMethodArgs[index] =
                                            methodResult.getQueryString().get(requestParam.value());

                                }
                            }
                        }
                    } else {

                        if (checkForNullQueryString(methodResult)) {

                            // can be null
                            formulatedMethodArgs[index] = null;

                        } else {

                            if (methodResult.getQueryString().get(paramName) == null) {

                                // can be null
                                formulatedMethodArgs[index] = null;

                            } else {

                                formulatedMethodArgs[index] = methodResult.getQueryString().get(paramName);

                            }
                        }
                    }
                }
                index++;
            } else {

                // object without annotation is a request body.
                formulatedMethodArgs[index] = operation.getBody();
                index++;
            }
        }
        return error;
    }

    private boolean checkForNullQueryString(URIMethodResult methodResult) {
        if (methodResult.getQueryString() == null)
            return true;

        return false;
    }

    private RestError getRestError(String message, String status) {
        RestError error;
        error = new RestError(
                message,
                status);
        return error;
    }
}
