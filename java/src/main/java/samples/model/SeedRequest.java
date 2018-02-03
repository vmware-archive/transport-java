package samples.model;

import com.vmware.bifrost.bridge.Request;
import io.swagger.client.model.Seed;

import java.util.Date;
import java.util.UUID;

public class SeedRequest extends Request<Seed> {


    public SeedRequest(Integer version, UUID uuid, String type, Seed payload) {
        super(version, uuid, type, payload);

    }

    public SeedRequest(UUID uuid, String type, Seed payload) {
        this(1, uuid, type, payload);
    }

    public SeedRequest(String type, Seed payload) {
        this(1, UUID.randomUUID(), type, payload);
    }

    public SeedRequest(String type) {
        this(1, UUID.randomUUID(), type, null);
    }

    @Override
    public String toString() {
        return "Seed Request: (" + this.getType() + ") [" + this.getId() + "]";
    }

}
