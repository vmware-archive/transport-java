package com.vmware.bifrost;

import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.bridge.util.Loggable;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

                    this.logInfoMessage("\uD83D\uDCE5","request received",message.getPayload().toString());
                    this.handleServiceRequest((ReqT)message.getPayload());
                },
                (Message message) -> {
                    logger.error("something went really wrong here");
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

//
//
//    public abstract void initializeSubscriptions();
//    protected abstract void handleServiceRequest(ReqT request);
//
//    protected String getName() {
//        return this.getClass().getTypeName();
//    }
//
//    @Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder.build();
//    }
//
//    @Override
//    public void registerServiceChannel(String serviceChannel) {
//        this.serviceChannel = serviceChannel;
//    }
//
//    protected void sendResponse(RespT response) {
//        this.bus.sendResponse(this.serviceChannel, response);
//    }
//
//    protected Observable callAPI(RestRequest restRequest) {
//        RestTemplate restTemplate = new RestTemplate();
//        ResponseEntity<String> response = restTemplate.getForEntity(restRequest.getURI(), String.class);
//
//    }

}