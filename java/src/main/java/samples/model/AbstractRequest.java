package samples.model;

import java.util.List;

public abstract class AbstractRequest<ReqT, ReqP> extends AbstractFrame {
    public AbstractRequest(Integer version) {
        super(version);
    }

    protected ReqT type;
    protected List<ReqP> payload;

    public AbstractRequest(ReqT type, List<ReqP> payload) {
        this(type, payload, 1);
    }

    public AbstractRequest(List<ReqP> payload, Integer version) {
        super(version);
        this.payload = payload;
    }

    public AbstractRequest(ReqT type, List<ReqP> payload, Integer version) {
        super(version);
        this.type = type;
        this.payload = payload;
    }

    public ReqT getType() {
        return this.type;
    }

    public List<ReqP> getPayload() {
        return this.payload;
    }

}
