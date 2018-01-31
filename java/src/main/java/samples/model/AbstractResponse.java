package samples.model;

import java.util.List;
import java.util.UUID;

public abstract class AbstractResponse<RespT> extends AbstractFrame {

    protected boolean error = false;
    protected int errorCode;
    protected String errorMessage;

    protected List<RespT> payload;

    public AbstractResponse(Integer version) {
        super(version);
    }

    public AbstractResponse(UUID uuid, List<RespT> payload) {
        this(uuid, payload, 1);
    }

    public AbstractResponse(UUID uuid, List<RespT> payload, Integer version) {
        super(version, uuid);
        this.payload = payload;
    }

    public AbstractResponse(UUID uuid, boolean error, Integer version) {
        super(version, uuid);
        this.error = error;
    }

    public AbstractResponse(boolean error) {
        super(1, UUID.randomUUID());
        this.error = error;
    }


    public List<RespT> getPayload() {
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

    public void setPayload(List<RespT> payload) {
        this.payload = payload;
    }
}
