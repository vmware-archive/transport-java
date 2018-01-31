package com.vmware.bifrost;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import samples.MockModel;
import samples.Mockable;
import samples.model.AbstractResponse;


import java.io.IOException;

/**
 * Copyright(c) VMware Inc. 2017-2018
 */
@BifrostService
public abstract class AbstractService<ReqT, RespT> extends Loggable implements Mockable, BifrostEnabled {

    @Autowired
    MessagebusService bus;

    @Autowired
    protected ResourceLoader resourceLoader;

    private ObjectMapper mapper = new ObjectMapper();
    private String serviceChannel;
    private BusTransaction serviceTransaction;
    private Resource res;
    protected boolean mockFail = false;

    public AbstractService(String serviceChannel) {
        super();
        this.serviceChannel = serviceChannel;
    }

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getServiceChannel() {
        return this.serviceChannel;
    }

    public void initializeSubscriptions() {

        this.serviceTransaction = this.bus.listenStream(this.serviceChannel,
                (Message message) -> {

                    this.logInfoMessage("\uD83D\uDCE5","Service Request Received",message.getPayload().toString());
                    this.handleServiceRequest((ReqT)message.getPayload());
                }
        );

        this.logInfoMessage("\uD83D\uDCE3", "initialized, handling requests on channel", this.serviceChannel);
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        this.serviceTransaction.unsubscribe();
    }

    public abstract void handleServiceRequest(ReqT request);

    public void sendResponse(RespT response) {
        this.bus.sendResponse(this.serviceChannel, response);
    }

    public void sendError(String message) {
        this.bus.sendError(this.serviceChannel, message);
    }


    public void apiFailedHandler(AbstractResponse response, ApiException e, String methodName) {
        response.setError(true);
        response.setErrorCode(e.getCode());
        response.setErrorMessage(e.getMessage());
        this.logErrorMessage("API call failed for " + methodName, e.getMessage());
        this.sendResponse((RespT)response);
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

}

