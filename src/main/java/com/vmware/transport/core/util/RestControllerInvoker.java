/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.transport.core.util;

import com.vmware.transport.core.error.RestError;
import com.vmware.transport.core.model.RestOperation;

import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Component
@SuppressWarnings("unchecked")
public class RestControllerInvoker {

    private final ApplicationContext context;

    RestControllerInvoker(ApplicationContext context) {
        this.context = context;
    }

    /**
     * Check method can be called and invoke accordingly.
     *
     * @param methodResult instance of a located method in a controller
     * @param operation rest operation submitted by requesting service.
     * @throws RuntimeException if the method cannot be executed because of a mismatch.
     */
    public void invokeMethod(URIMethodResult methodResult, RestOperation operation) {

        Object[] formulatedMethodArgs = new Object[methodResult.getMethogArgList().size()];

        if (methodResult.getMethodArgs().size() >= 1) {

            Map<String, Object> pathItemMap = methodResult.getPathItemMap();
            Map<String, Class> methodArgs = methodResult.getMethodArgs();
            Map<String, Class> methodAnnotationTypes = methodResult.getMethodAnnotationTypes();


            // check if path items and method args match
            if (pathItemMap.size() >= 1) {

                if (doPathItemsAndMethodArgsMatch(pathItemMap, methodArgs, methodAnnotationTypes)) {
                    processAndCallMethod(methodResult, operation, formulatedMethodArgs, pathItemMap);


                } else {

                    operation.getErrorHandler().accept(
                            getRestError(
                                    "Supplied method can't be used, the params and path items don't align.",
                                    500)
                    );
                }

            } else {

                processAndCallMethod(methodResult, operation, formulatedMethodArgs, pathItemMap);
            }

        } else {

            callControllerMethod(methodResult, operation, null);
        }
    }

    private void processAndCallMethod(URIMethodResult methodResult, RestOperation operation, Object[] formulatedMethodArgs, Map<String, Object> pathItemMap) {
        RestError error;// iterate through all our data and construct the correct signature to call.
        error = processMethod(methodResult, pathItemMap, formulatedMethodArgs, operation);

        // send error instead of invoking method, bypass completely.
        if (error != null) {
            operation.getErrorHandler().accept(error);
            return;
        }

        callControllerMethod(methodResult, operation, formulatedMethodArgs);
    }

    /**
     * Validate path and method arguments align
     *
     * @param pathItemMap       map of the path items and the instance (string, number UUID)
     * @param methodArgs        a map of every argument to the method and the type
     * @param methodAnnotations a map of every method annotation.
     * @return true if path items and method arguments match (for path variables only)
     */
    private boolean doPathItemsAndMethodArgsMatch(
            Map<String, Object> pathItemMap,
            Map<String, Class> methodArgs,
            Map<String, Class> methodAnnotations
    ) {

        boolean match = false;

        for (String pathItem : pathItemMap.keySet()) {

            String className = methodAnnotations.get(pathItem).getName();
            if (className.contentEquals("org.springframework.web.bind.annotation.PathVariable")) {
                match = methodArgs.get(pathItem) != null;
            }
        }
        return match;
    }

    private void callControllerMethod(URIMethodResult methodResult, RestOperation operation, Object[] formulatedMethodArgs) {
        Method method = methodResult.getMethod();
        Object bean = context.getBean(methodResult.getController().getClass());
        try {
            if (formulatedMethodArgs != null) {
                operation.getSuccessHandler().accept(
                        method.invoke(bean, formulatedMethodArgs)
                );
            } else {
                operation.getSuccessHandler().accept(
                        method.invoke(bean)
                );
            }

        } catch (InvocationTargetException e) {
            if (e.getTargetException().getClass().equals(AuthenticationCredentialsNotFoundException.class)) {
                operation.getErrorHandler().accept(
                        this.getRestError(e.getTargetException().getMessage(), 500)
                );
            } else {
                operation.getErrorHandler().accept(
                        this.getRestError(e.getTargetException().getMessage(), 401)
                );
            }

        } catch (IllegalAccessException e) {
            operation.getErrorHandler().accept(
                    this.getRestError("Method cannot be called, method param mismatch", 500)
            );
        } catch (IllegalArgumentException e) {
            operation.getErrorHandler().accept(
                    this.getRestError("Method cannot be called, method param types don't match", 500)
            );
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

                // check RequestHeader argument.
                if (methodResult.getMethodAnnotationTypes().get(paramName).equals(RequestHeader.class)) {

                    RequestHeader requestHeader = (RequestHeader) methodResult.getMethodAnnotationValues().get(paramName);
                    if (requestHeader != null && !requestHeader.value().isEmpty()) {
                        if (requestHeader.required()) {

                            if (!operation.getHeaders().containsKey(requestHeader.value())) {
                                error = getRestError(
                                        "Method requires headers parameters, however no header with key '"
                                                + requestHeader.value() + "' was found",
                                        500);
                            } else {

                                formulatedMethodArgs[index] = operation.getHeaders().get(requestHeader.value());

                            }

                        } else {

                            if (!operation.getHeaders().containsKey(paramName)) {

                                formulatedMethodArgs[index] = null;

                            } else {

                                formulatedMethodArgs[index] = operation.getHeaders().get(paramName);

                            }
                        }
                    } else {

                        // no value supplied, use param name as key.

                        if (!operation.getHeaders().containsKey(paramName)) {

                            error = getRestError(
                                    "Method requires headers parameters, however no header with key '"
                                            + paramName + "' was found",
                                    500);

                        } else {

                            formulatedMethodArgs[index] = operation.getHeaders().get(paramName);

                        }
                    }
                }


                if (methodResult.getMethodAnnotationTypes().get(paramName).equals(RequestParam.class)) {

                    RequestParam requestParam = (RequestParam) methodResult.getMethodAnnotationValues().get(paramName);

                    if (requestParam != null && !requestParam.value().isEmpty()) {
                        if (requestParam.required()) {
                            if (methodResult.getQueryString() == null) {

                                error = getRestError(
                                        "Method requires request parameters, however none have been supplied.",
                                        500
                                );

                            } else {

                                if (methodResult.getQueryString().get(requestParam.value()) == null) {

                                    error = getRestError(
                                            "Method requires request param '" + requestParam.value()
                                                    + "', This maps to method argument '" + paramName
                                                    + "', but wasn't supplied with URI properties.",
                                            500);

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
        return methodResult.getQueryString() == null;
    }

    private RestError getRestError(String message, Integer status) {
        RestError error;
        error = new RestError(
                message,
                status);
        return error;
    }
}
