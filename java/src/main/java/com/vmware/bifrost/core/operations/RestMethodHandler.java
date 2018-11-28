package com.vmware.bifrost.core.operations;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class RestMethodHandler<Req, Resp> {

    private Map<String, Consumer<Request>> methodBeforeHandlers;
    private Map<String, Consumer<Response>> methodAfterHandlers;
    private Map<String, Function<Request, Response>> dropInMethodHandlers;

    public Consumer<Response> getRunAfterForMethod(String methodName) {
        return methodAfterHandlers.get(methodName);
    }

    public Function<Request, Response> getDropInMethod(String methodName) {
        return dropInMethodHandlers.get(methodName);
    }

    public Consumer<Request> getRunBeforeForMethod(String methodName) {
        return methodBeforeHandlers.get(methodName);
    }

    public void setRunBeforeMethod(String methodName, Consumer<Request> request) {
        this.methodBeforeHandlers.put(methodName, request);
    }

    public void setRunAfterMethod(String methodName, Consumer<Response> response) {
        this.methodAfterHandlers.put(methodName, response);
    }

    public void setDropInMethod(String methodName, Function<Request, Response> response) {
        this.dropInMethodHandlers.put(methodName, response);
    }

    public RestMethodHandler(String methodName, Consumer<Request> runBefore, Consumer<Response> runAfter, Function<Request, Response> dropIn) {
        methodBeforeHandlers = new HashMap<>();
        methodAfterHandlers = new HashMap<>();
        dropInMethodHandlers = new HashMap<>();
        if (runBefore != null) {
            methodBeforeHandlers.put(methodName, runBefore);
        }
        if (runAfter != null) {
            methodAfterHandlers.put(methodName, runAfter);
        }
        if (dropInMethodHandlers != null) {
            dropInMethodHandlers.put(methodName, dropIn);
        }
    }
}