package samples;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;


import java.io.IOException;

/**
 * Copyright(c) VMware Inc. 2017-2018
 */
@BifrostService
public abstract class AbstractService extends Loggable implements Mockable, BifrostEnabled {

    @Autowired
    MessagebusService bus;

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
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

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

                        this.handleServiceRequest((Request)message.getPayload());

                    } catch (ClassCastException cce) {
                        cce.printStackTrace();
                        this.logErrorMessage("Service unable to process request, " +
                                "request cannot be cast",  message.getPayload().getClass().getSimpleName());
                        throw new RequestException("Service unable to process request, request cannot be cast");

                    }
                }
        );

        this.logInfoMessage("\uD83D\uDCE3", "initialized, handling requests on channel", this.serviceChannel);
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        this.serviceTransaction.unsubscribe();
    }

    public abstract void handleServiceRequest(Request request);

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
        return (T)this.mapper.convertValue(request.getPayload(), clazz);
    }


}

