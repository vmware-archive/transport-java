package com.vmware.bifrost;

import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bus.MessagebusService;
import com.vmware.bifrost.bus.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Copyright(c) VMware Inc. 2017
 */
@Service
public abstract class AbstractService<ReqT, RespT> implements BifrostEnabled {

    @Autowired
    MessagebusService bus;

    protected String serviceChannel;

    public AbstractService(String serviceChannel) {
        this.serviceChannel = serviceChannel;
    }

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public String getServiceChannel() {
        return this.serviceChannel;
    }

    public void initializeSubscriptions() {

        this.bus.listenStream(this.serviceChannel,
                (Message message) -> {
                    this.handleServiceRequest((ReqT)message.getPayload());
                }
        );


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