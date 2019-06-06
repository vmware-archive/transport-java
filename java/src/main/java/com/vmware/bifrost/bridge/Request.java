package com.vmware.bifrost.bridge;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Request<ReqP> extends AbstractFrame {

    @Getter @Setter
    private String targetUser;
    @Getter @Setter
    private String request;
    @Getter @Setter
    private String channel;

    private Boolean isRejected = false;

    public Request() {}

    public Request(Integer version, UUID id, String request, ReqP payload) {
        super(version, id, payload);
        this.request = request;
    }

    // We need to store the requestr channel because we need to respond there
    public Request(Integer version, UUID id, String request, ReqP payload, String channel) {
        this(version, id, request, payload);
        this.channel = channel;
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

    public String toString() {
        return "Request ID: " + this.getId();
    }

    public Boolean getRejected() {
        return isRejected;
    }

    public void setRejected(Boolean rejected) {
        isRejected = rejected;
    }
}
