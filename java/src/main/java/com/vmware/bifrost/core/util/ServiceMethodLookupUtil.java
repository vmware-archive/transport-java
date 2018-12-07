/*
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.util;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.core.interfaces.CustomServiceCode;
import com.vmware.bifrost.core.interfaces.CustomServiceCodeHandler;
import com.vmware.bifrost.core.interfaces.ServiceMethodHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;


@Component
public class ServiceMethodLookupUtil extends Loggable {

    @Autowired
    private ApplicationContext context;
    private final Map<String, ServiceMethodHandler> customMethodHandlers = new HashMap<>();

    /**
     * Scan Application for service method handlers, extract them and cache them.
     */
    public void loadCustomHandlers() {
        Collection<Object> customHandlerObjects = context.getBeansWithAnnotation(CustomServiceCode.class).values();
        for (Object service : customHandlerObjects) {

            final CustomServiceCode serviceAnnotation = service.getClass().getAnnotation(CustomServiceCode.class);
            if (serviceAnnotation.serviceName().equals(this.getClass().getSimpleName())) {
                this.logInfoMessage("⚙️", "Loading custom code handler for service " + serviceAnnotation.serviceName() + " provided by", service.getClass().getSimpleName());
            }

            for (Method method : service.getClass().getDeclaredMethods()) {
                CustomServiceCodeHandler annotation = method.getAnnotation(CustomServiceCodeHandler.class);
                if (annotation != null) {

                    // Register a handler for this method
                    this.logDebugMessage("Registering method [" + method.getName() + "()] mapped to method [" + annotation.methodName() + "()] for stage ", annotation.stage().toString());
                    switch (annotation.stage()) {

                        case Before:
                            registerBeforeHandler(
                                    serviceAnnotation.serviceName(),
                                    annotation.methodName(),
                                    frame -> handleCustomMethod(service, method, frame));
                            break;

                        case After:
                            registerAfterHandler(
                                    serviceAnnotation.serviceName(),
                                    annotation.methodName(),
                                    frame -> handleCustomMethod(service, method, frame));
                            break;

                        case DropIn:
                            registerDropInHandler(
                                    serviceAnnotation.serviceName(),
                                    annotation.methodName(),
                                    frame -> handleCustomDropInMethod(service, method, frame));
                            break;

                    }
                }
            }
        }
    }

    public Response handleCustomDropInMethod(Object container, Method method, Object data) {
        try {
            return (Response) method.invoke(container, data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerBeforeHandler(String service, String method, Consumer<Request> request) {
        if (customMethodHandlers.containsKey(service)) {
            ServiceMethodHandler handler = customMethodHandlers.get(service);
            handler.setRunBeforeMethod(method, request);
        } else {
            customMethodHandlers.put(service, new ServiceMethodHandler(method, request, null, null));
        }
    }

    private void registerAfterHandler(String service, String method, Consumer<Response> response) {
        if (customMethodHandlers.containsKey(service)) {
            ServiceMethodHandler handler = customMethodHandlers.get(service);
            handler.setRunAfterMethod(method, response);
        } else {
            customMethodHandlers.put(service, new ServiceMethodHandler(method, null, response, null));
        }
    }

    private void registerDropInHandler(String service, String method, Function<Request, Response> func) {
        if (customMethodHandlers.containsKey(service)) {
            ServiceMethodHandler handler = customMethodHandlers.get(service);
            handler.setDropInMethod(method, func);
        } else {
            customMethodHandlers.put(service, new ServiceMethodHandler(method, null, null, func));
        }
    }

   public void runCustomCodeBefore(String serviceName, String methodName, Request request) {
        if (customMethodHandlers.containsKey(serviceName)) {
            ServiceMethodHandler handler = customMethodHandlers.get(serviceName);
            if (handler.getRunBeforeForMethod(methodName) != null) {
                this.logDebugMessage("Running custom code pre handling for service [" + serviceName + "] method", methodName);
                handler.getRunBeforeForMethod(methodName).accept(request);
            } else {
                this.logTraceMessage("Skipping pre handling custom code for [" + serviceName + "]", "no handler for method " + methodName);
            }
        } else {
            this.logTraceMessage("Skipping pre handling custom code for [" + serviceName + "]", "no handlers registered");
        }
    }

    public void runCustomCodeAfter(String serviceName, String methodName, Response response) {
        if (customMethodHandlers.containsKey(serviceName)) {
            ServiceMethodHandler handler = customMethodHandlers.get(serviceName);
            if (handler.getRunAfterForMethod(methodName) != null) {
                this.logDebugMessage("Running custom code post handling for service [" + serviceName + "] method", methodName);
                handler.getRunAfterForMethod(methodName).accept(response);
            } else {
                this.logTraceMessage("Skipping post handling custom code for [" + serviceName + "]", "no handler for method " + methodName);
            }
        } else {
            this.logTraceMessage("Skipping post handling custom code for [" + serviceName + "]", "no handlers registered");
        }
    }

    public Response runCustomCodeAndReturnResponse(String serviceName, String methodName, Request request) {
        Response response = null;
        if (customMethodHandlers.containsKey(serviceName)) {
            ServiceMethodHandler handler = customMethodHandlers.get(serviceName);
            if (handler.getDropInMethod(methodName) != null) {
                this.logDebugMessage("Running drop in custom code for service [" + serviceName + "] method", methodName);
                response = handler.getDropInMethod(methodName).apply(request);
            } else {
                this.logTraceMessage("Skipping drop in custom code for [" + serviceName + "]", "no handler for method " + methodName);
            }
        } else {
            this.logTraceMessage("Skipping drop in custom code for [" + serviceName + "]", "no handlers registered");
        }
        return response;
    }

    private void handleCustomMethod(Object container, Method method, Object data) {
        try {
            method.invoke(container, data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


//    public void apiFailedHandler(Response response, ApiException e, String methodName) {
//        response.setError(true);
//        response.setErrorCode(e.getCode());
//        response.setErrorMessage(e.getMessage());
//        this.logErrorMessage("API call failed for " + methodName, e.getMessage());
//        this.sendResponse(response);
//    }
//
//
//    protected <T> T getModels(Class<T> clazz) throws IOException {
//        return this.getModels(clazz, mapper, res);
//    }
//
//    protected MockModel mockModel;
//    protected void loadSampleModels() {
//
//        this.logDebugMessage("Loading sample mock models.");
//        res = this.loadResources(this.resourceLoader);
//        try {
//            mockModel = this.getModels(MockModel.class);
//        } catch (IOException e) {
//            this.logErrorMessage("Unable to load mock model data", e.getMessage());
//        }
//    }

}
