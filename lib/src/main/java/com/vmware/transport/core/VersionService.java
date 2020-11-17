/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */
package com.vmware.transport.core;

import com.vmware.transport.bridge.Request;
import com.vmware.transport.bridge.Response;
import com.vmware.transport.bus.model.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * Version Service returns the current version of the Transport running in the fabric, which is essentially
 * the version of the fabric we're running.
 */
@Component
@PropertySource(value="classpath:transport.properties", ignoreResourceNotFound=true)
public class VersionService extends AbstractService<Request<String>, Response<String>> {

    VersionService() {
        super(CoreChannels.VersionService);
    }

    // Return "local.dev" if the transport.properties file is missing from the classpath.
    // This can happen if the sample Application was started from the IDE as the
    // transport.properties file is generated during the gradle build.
    @Value("${transport.version:local.dev}")
    private String transportVersion;

    protected void handleServiceRequest(Request request, Message busMessage) {
        switch (request.getRequest()) {
            case VersionCommands.Version:
                handleVersionRequest(request);
                break;

            default:
                this.handleUnknownRequest(request);
        }
    }

    private void handleVersionRequest(Request request) {
        Response<String> response = new Response<>(request.getId(), transportVersion);
        this.sendResponse(response, request.getId());
    }
}

class VersionCommands {
    final static String Version = "version";
}
