package com.vmware.bifrost.bridge;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class Response<RespP> extends AbstractFrame<RespP> {

    @Getter @Setter
    protected boolean error = false;

    @Getter @Setter
    protected int errorCode;

    @Getter
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

<<<<<<< Updated upstream
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

=======
>>>>>>> Stashed changes
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.message = errorMessage; // make this compatible with GeneralError in TS bifrost.
    }

    public String toString() {
        return "Response ID: " + this.getId();
    }
}
