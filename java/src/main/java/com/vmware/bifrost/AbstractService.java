package com.vmware.bifrost;

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
import samples.model.AbstractRequest;
import samples.model.AbstractResponse;
import samples.model.SeedRequest;
import samples.model.SeedResponse;

/**
 * Copyright(c) VMware Inc. 2017
 */
@BifrostService
public abstract class AbstractService<ReqT, RespT> extends Loggable implements BifrostEnabled {

    @Autowired
    MessagebusService bus;

    protected String serviceChannel;
    protected BusTransaction serviceTransaction;

    public AbstractService(String serviceChannel) {
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
                },
                (Message message) -> {
                    super.logErrorMessage("API call failed for getSeeds()", message.toString());
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


    protected void apiFailedHandler(AbstractResponse response, ApiException e) {
        response.setError(true);
        response.setErrorCode(e.getCode());
        response.setErrorMessage(e.getMessage());
        super.logErrorMessage("API call failed for getSeeds()", response.getErrorMessage());
        this.sendResponse((RespT)response);
    }

}