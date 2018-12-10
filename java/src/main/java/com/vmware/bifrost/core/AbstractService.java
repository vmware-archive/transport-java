/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.bifrost.bridge.spring.BifrostEnabled;
import com.vmware.bifrost.bridge.spring.BifrostService;
import com.vmware.bifrost.bus.BusTransaction;
import com.vmware.bifrost.core.util.Loggable;
import com.vmware.bifrost.bus.EventBus;
import com.vmware.bifrost.bus.model.Message;
import com.vmware.bifrost.core.util.ServiceMethodLookupUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ResourceLoader;
import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;

import java.util.UUID;


@BifrostService
public abstract class AbstractService<RequestType extends Request, ResponseType extends Response>
        extends Loggable implements BifrostEnabled {

    @Autowired
    EventBus bus;

    @Autowired
    protected ApplicationContext context;

    @Autowired
    protected ResourceLoader resourceLoader;

    protected ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected ServiceMethodLookupUtil methodLookupUtil;

    protected String serviceChannel;
    private BusTransaction serviceTransaction;

    public AbstractService(String serviceChannel) {
        super();
        this.serviceChannel = serviceChannel;
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
    }

    public String getServiceChannel() {
        return this.serviceChannel;
    }

    public void initializeSubscriptions() {

        this.serviceTransaction = this.bus.listenRequestStream(this.serviceChannel,
                (Message message) -> {
                    try {

                        this.logInfoMessage(
                                "\uD83D\uDCE5",
                                "Service Request Received",
                                message.getPayload().toString());

                        this.handleServiceRequest((RequestType) message.getPayload());

                    } catch (ClassCastException cce) {
                        this.logErrorMessage("Service unable to process request, " +
                                "request cannot be cast", message.getPayload().getClass().getSimpleName());

                        Response resp = new Response(UUID.randomUUID(), message.getPayload());
                        resp.setError(true);
                        resp.setErrorCode(500);
                        resp.setErrorMessage(this.getClass().getSimpleName()
                                + " cannot handle request, payload isn't derived from 'Request', type: "
                                + message.getPayload().getClass().getSimpleName());

                        this.sendError(resp);

                    }
                },
                (Message message) -> {
                    this.logDebugMessage("ignore!" + message.getPayload());
                }

        );

        this.logInfoMessage("\uD83D\uDCE3", this.getClass().getSimpleName()
                + " initialized, handling requests on channel", this.serviceChannel);
        this.methodLookupUtil.loadCustomHandlers();
    }

    protected abstract void handleServiceRequest(RequestType request);


    protected void sendResponse(ResponseType response) {
        this.logInfoMessage(
                "\uD83D\uDCE4",
                "Sending Service Response",
                response.toString());
        this.bus.sendResponseMessage(this.serviceChannel, response);
    }

    protected <E extends Response> void  sendError(E error) {
        this.bus.sendErrorMessage(this.serviceChannel, error);
    }

    protected <T> T castPayload(Class clazz, Request request) throws ClassCastException {
        return (T) this.mapper.convertValue(request.getPayload(), clazz);
    }

}

