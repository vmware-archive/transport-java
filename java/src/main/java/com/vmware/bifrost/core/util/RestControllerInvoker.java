/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.core.model.RestOperation;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
public class RestControllerInvoker {

    RestControllerInvoker() {

    }

    public void invokeMethod(URIMethodResult methodResult, RestOperation operation) throws RuntimeException {

        if (methodResult.getMethodArgs().size() >= 1) {

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

}
