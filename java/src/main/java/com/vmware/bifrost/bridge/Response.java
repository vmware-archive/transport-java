package com.vmware.bifrost.bridge;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Response<RespP> extends AbstractFrame {

    protected boolean error = false;
    protected int errorCode;
    protected String errorMessage;
    protected List<RespP> payload;

    public Response(Integer version, UUID uuid, List<RespP> payload) {
        super(version, uuid);
        this.payload = payload;
    }

    public Response(UUID uuid, List<RespP> payload) {
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

    public List<RespP> getPayload() {
        return this.payload;
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
