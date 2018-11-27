/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.RequestException;
import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import io.swagger.client.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import samples.*;


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@BifrostService
public abstract class AbstractService extends Loggable
        implements Mockable, BifrostEnabled, ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    MessagebusService bus;

    @Autowired
    private ConfigurableApplicationContext context;

    private final Map<String, ServiceMethodHandler> customMethodHandlers = new HashMap<>();

    @Autowired
    protected ResourceLoader resourceLoader;
    protected ObjectMapper mapper = new ObjectMapper();

    private String serviceChannel;
    private BusTransaction serviceTransaction;
    private Resource res;
    public boolean mockFail = false;

    public AbstractService(String serviceChannel) {
        super();
        this.serviceChannel = serviceChannel;
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }

    //protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    //protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getServiceChannel() {
        return this.serviceChannel;
    }

    public void initializeSubscriptions() {

        this.serviceTransaction = this.bus.listenStream(this.serviceChannel,
                (Message message) -> {
                    try {

                        this.logInfoMessage(
                                "\uD83D\uDCE5",
                                "Service Request Received",
                                message.getPayload().toString());

                        this.handleServiceRequest((Request) message.getPayload());

                    } catch (ClassCastException cce) {
                        cce.printStackTrace();
                        this.logErrorMessage("Service unable to process request, " +
                                "request cannot be cast", message.getPayload().getClass().getSimpleName());
                        throw new RequestException("Service unable to process request, request cannot be cast");

                    }
                }
        );

        this.logInfoMessage("\uD83D\uDCE3", "initialized, handling requests on channel", this.serviceChannel);
        this.loadCustomHandlers();
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        this.serviceTransaction.unsubscribe();
    }

    public void handleServiceRequest(Request request) {
        throw new RuntimeException("Service must implement handleServiceRequest()");
    }

    public void sendResponse(Response response) {
        this.logInfoMessage(
                "\uD83D\uDCE4",
                "Sending Service Response",
                response.toString());
        this.bus.sendResponse(this.serviceChannel, response);
    }

    public void sendError(String message) {
        this.bus.sendError(this.serviceChannel, message);
    }


    public void apiFailedHandler(Response response, ApiException e, String methodName) {
        response.setError(true);
        response.setErrorCode(e.getCode());
        response.setErrorMessage(e.getMessage());
        this.logErrorMessage("API call failed for " + methodName, e.getMessage());
        this.sendResponse(response);
    }


    protected <T> T getModels(Class<T> clazz) throws IOException {
        return this.getModels(clazz, mapper, res);
    }

    protected MockModel mockModel;

    protected void loadSampleModels() {

        this.logDebugMessage("Loading sample mock models.");
        res = this.loadResources(this.resourceLoader);
        try {
            mockModel = this.getModels(MockModel.class);
        } catch (IOException e) {
            this.logErrorMessage("Unable to load mock model data", e.getMessage());
        }
    }

    protected <T> T castPayload(Class clazz, Request request) throws ClassCastException {
        return (T) this.mapper.convertValue(request.getPayload(), clazz);
    }

    public void onApplicationEvent(ContextRefreshedEvent event) {

        //this.logErrorMessage("duck man", null);
    }

    private void handleCustomMethod(Object container, Method method, Object data) {
        try {
            method.invoke(container, data);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private Response handleCustomDropInMethod(Object container, Method method, Object data) {
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

    protected void runCustomCodeBefore(String serviceName, String methodName, Request request) {
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

    protected void runCustomCodeAfter(String serviceName, String methodName, Response response) {
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

    protected Response runCustomCodeAndReturnResponse(String serviceName, String methodName, Request request) {
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


    protected void loadCustomHandlers() {
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
}

