package com.vmware.bifrost.bridge;

import java.util.UUID;

public class Request<ReqP> extends AbstractFrame {


    private String request;

    public Request() {}

    public Request(Integer version, UUID id, String request, ReqP payload) {
        super(version, id, payload);
        this.request = request;
    }

    public Request(UUID id, String request, ReqP payload) {
        this(1, id, request, payload);
    }

    public Request(String request, ReqP payload) {
        this(1, UUID.randomUUID(), request, payload);
    }

    public Request(String request) {
        this(1, UUID.randomUUID(), request, null);
    }

    public Request(UUID id, String request) {
        this(1, id, request, null);
    }

    public String getRequest() {
        return this.request;
    }

    public String toString() {
        return "Request ID: " + this.getId();
    }

    public void setRequest(String request) {
        this.request = request;
    }

}
