/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.core.error.GeneralError;
import com.vmware.bifrost.core.model.RestOperation;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

@Component
public class RestControllerInvoker {

    RestControllerInvoker() {

    }

    public void invokeMethod(URIMethodResult methodResult, RestOperation operation) throws RuntimeException {

        if (methodResult.getMethodArgs().size() >= 1) {

            Map<String, String> pathItemMap = methodResult.getPathItemMap();
            Map<String, Class> methodArgs = methodResult.getMethodArgs();
            Map<String, Class> methodAnnotationTypes = methodResult.getMethodAnnotationTypes();

            // check if path items and method args match
            if (doPathItemsAndMethodArgsMatch(pathItemMap, methodArgs, methodAnnotationTypes)) {

                String requestParamArgName = methodResult.getRequestParamArgumentName();
                String requestBodyArgName = methodResult.getRequestBodyArgumentName();

                Object[] formulatedMethodArgs = new Object[methodResult.getMethogArgList().size()];
                GeneralError error = null;

                // iterate through all our data and construct the correct signature to call.
                int index = 0;
                for (String paramName : methodResult.getMethogArgList()) {

                    if (methodResult.getMethodAnnotationTypes().get(paramName).equals(PathVariable.class)) {
                        formulatedMethodArgs[index] = pathItemMap.get(paramName);
                    }

                    if (methodResult.getMethodAnnotationTypes().get(paramName).equals(RequestParam.class)) {

                        RequestParam requestParam = (RequestParam) methodResult.getMethodAnnotationValues().get(paramName);

                        if (requestParam != null && !requestParam.value().isEmpty()) {

                            if(!requestParam.required() && methodResult.getQueryString() != null) {
                                formulatedMethodArgs[index] = methodResult.getQueryString().get(requestParam.value());
                            } else {
                                error = new GeneralError("REST Error","Unable to call, missing query params!");

                            }


                        } else {

                            if(!requestParam.required() && methodResult.getQueryString() != null) {
                                formulatedMethodArgs[index] = methodResult.getQueryString().get(paramName);
                            } else {
                                error = new GeneralError("REST Error","Unable to call, missing query params!");

                            }


                        }
                    }
                    index++;

                }

                Method method = methodResult.getMethod();
                Object controller = methodResult.getController();

                try {

                    operation.getSuccessHandler().accept(method.invoke(controller, formulatedMethodArgs));

                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }



               // operation.getSuccessHandler().accept("funky spunky!" + requestParamArgName);

            } else {

                operation.getSuccessHandler().accept("sad munkey :(");
            }


        } else {
            Method method = methodResult.getMethod();
            Object controller = methodResult.getController();

            try {

                operation.getSuccessHandler().accept(method.invoke(controller));

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }


        }
    }

    public boolean doPathItemsAndMethodArgsMatch(
            Map<String, String> pathItemMap,
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

}
