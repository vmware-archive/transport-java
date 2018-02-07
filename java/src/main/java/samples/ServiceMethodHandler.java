package samples;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ServiceMethodHandler {

    private Map<String, Consumer<Request>> methodBeforeHandlers;
    private Map<String, Consumer<Response>> methodAfterHandlers;

    public Consumer<Response> getRunAfterForMethod(String methodName) {
        return methodAfterHandlers.get(methodName);
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

    ServiceMethodHandler(String methodName, Consumer<Request> runBefore, Consumer<Response> runAfter) {
        methodBeforeHandlers = new HashMap<>();
        methodAfterHandlers = new HashMap<>();
        if (runBefore != null) {
            methodBeforeHandlers.put(methodName, runBefore);
        }
        if (runAfter != null) {
            methodAfterHandlers.put(methodName, runAfter);
        }
    }
}
