/*
 * Copyright(c) VMware Inc. 2019. All rights reserved.
 */
package com.vmware.bifrost.core;

import com.vmware.bifrost.bridge.Request;
import com.vmware.bifrost.bridge.Response;
import com.vmware.bifrost.bus.model.Message;
import org.springframework.stereotype.Component;

/**
 * Version Service returns the current version of the Bifröst running in the fabric, which is essentially
 * the version of the fabric we're running.
 */
@Component
public class VersionService extends AbstractService<Request<String>, Response<String>> {

    VersionService() {
        super(CoreChannels.VersionService);
    }

    protected void handleServiceRequest(Request request, Message busMessage) {
        switch (request.getRequest()) {
            case VersionCommands.Version:
                handleVersionRequest(request);
                break;

            default:
                this.handleUnknownRequest(request);
        }
    }

    // TODO: Engineer a way to bring the current version into AbstractService
    // ideally this is read from spring application properties or similar - that was
    // patched with the version from version.txt
    // right now, this is returning a hard coded value, to get us rolling.
    private void handleVersionRequest(Request request) {
        Response<String> response = new Response<>(request.getId(), "0.0.13");
        this.sendResponse(response, request.getId());
    }
}

class VersionCommands {
    final static String Version = "version";
}
