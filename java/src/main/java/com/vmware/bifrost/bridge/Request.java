package com.vmware.bifrost.bridge;

import java.util.UUID;

public class Request<ReqP> extends AbstractFrame {

    private ReqP payload;
    private String type;

    public Request() {}

    public Request(Integer version, UUID id, String type, ReqP payload) {
        super(version, id);
        this.payload = payload;
        this.type = type;
    }

    public Request(UUID id, String type, ReqP payload) {
        this(1, id, type, payload);
    }

    public Request(String type, ReqP payload) {
        this(1, UUID.randomUUID(), type, payload);
    }

    public Request(String type) {
        this(1, UUID.randomUUID(), type, null);
    }

    public String getType() {
        return this.type;
    }

    public ReqP getPayload() {
        return this.payload;
    }

    public String toString() {
        return "Request ID: " + this.getId();
    }

    public void setPayload(ReqP payload) {
        this.payload = payload;
    }

    public void setType(String type) {
        this.type = type;
    }

}
