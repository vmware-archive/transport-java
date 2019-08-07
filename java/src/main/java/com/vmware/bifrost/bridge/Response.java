package com.vmware.bifrost.bridge;

import java.util.UUID;

public class Response<RespP> extends AbstractFrame<RespP> {

    protected boolean error = false;
    protected int errorCode;
    protected String errorMessage;

    public Response(Integer version, UUID uuid, RespP payload) {
        super(version, uuid, payload);
    }

    public Response(UUID uuid, RespP payload) {
        this(1, uuid, payload);
    }

    public Response(Integer version, UUID uuid, boolean error) {
        this(version, uuid, null);
        this.error = error;
    }

    public Response(UUID uuid, boolean error) {
        this(1, uuid, null);
        this.error = error;
    }

    public Response() {
        // needed for deserialization
        this(UUID.randomUUID(), false);
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String toString() {
        return "Response ID: " + this.getId();
    }

}
