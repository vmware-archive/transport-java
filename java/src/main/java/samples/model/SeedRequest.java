package samples.model;

import io.swagger.client.model.Seed;

public class SeedRequest extends AbstractRequest<SeedRequest.Type, Seed> {

    public enum Type {
        PlantSeed,
        GetSeeds,
        KillPlant
    }

    public SeedRequest(Type type) {
        this(type, null, 1);
    }

    public SeedRequest(Type type, Seed payload) {
        this(type, payload, 1);
    }

    public SeedRequest(Type type, Seed payload, Integer version) {
        super(type, payload, version);
    }

    @Override
    public String toString() {
        return "Seed Request: (" + this.getType() + ") [" + this.getUuid() + "]";
    }

}
