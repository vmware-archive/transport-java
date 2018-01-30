package samples.model;

import java.util.List;
import java.util.UUID;

public abstract class AbstractResponse<RespT> extends AbstractFrame {
    public AbstractResponse(Integer version) {
        super(version);
    }
    protected List<RespT> payload;

    public AbstractResponse(UUID uuid, List<RespT> payload) {
        this(uuid, payload, 1);
    }

    public AbstractResponse(UUID uuid, List<RespT> payload, Integer version) {
        super(version, uuid);
        this.payload = payload;
    }

    public List<RespT> getPayload() {
        return this.payload;
    }
}
