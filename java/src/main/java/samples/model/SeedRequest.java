package samples.model;

import com.vmware.bifrost.bridge.Request;
import io.swagger.client.model.Seed;

import java.util.Date;
import java.util.UUID;

public class SeedRequest extends Request<Seed> {

//    public enum Type {
//        PlantSeed,
//        GetSeeds,
//        KillPlant
//    }

    public SeedRequest(Integer version, UUID uuid, Date created, String type, Seed payload) {
        super(version, uuid, created, type, payload);
    }

    public SeedRequest(String type) {
        super(1, UUID.randomUUID(), new Date(), type, null);
    }

    public SeedRequest(String type, Seed payload) {
        super(1, UUID.randomUUID(), new Date(), type, payload);
    }

    @Override
    public String toString() {
        return "Seed Request: (" + this.getType() + ") [" + this.getId() + "]";
    }

}
