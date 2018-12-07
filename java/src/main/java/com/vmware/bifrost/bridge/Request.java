package com.vmware.bifrost.bridge;

import java.util.UUID;

public class Request<ReqP> extends AbstractFrame {

    private ReqP payload;
    private String command;

    public Request() {}

    public Request(Integer version, UUID id, String command, ReqP payload) {
        super(version, id);
        this.payload = payload;
        this.command = command;
    }

    public Request(UUID id, String command, ReqP payload) {
        this(1, id, command, payload);
    }

    public Request(String command, ReqP payload) {
        this(1, UUID.randomUUID(), command, payload);
    }

    public Request(String command) {
        this(1, UUID.randomUUID(), command, null);
    }

    public String getCommand() {
        return this.command;
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

    public void setCommand(String command) {
        this.command = command;
    }

}
