package samples.model;

public abstract class AbstractRequest<ReqT, ReqP> extends AbstractFrame {
    public AbstractRequest(Integer version) {
        super(version);
    }

    protected ReqT type;
    protected ReqP payload;

    public AbstractRequest(ReqT type, ReqP payload) {
        this(type, payload, 1);
    }

    public AbstractRequest(ReqP payload, Integer version) {
        super(version);
        this.payload = payload;
    }

    public AbstractRequest(ReqT type, ReqP payload, Integer version) {
        super(version);
        this.type = type;
        this.payload = payload;
    }

    public ReqT getType() {
        return this.type;
    }

    public ReqP getPayload() {
        return this.payload;
    }

}
