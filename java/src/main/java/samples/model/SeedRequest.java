package samples.model;

import io.swagger.client.model.Seed;

import java.util.List;

public class SeedRequest extends AbstractRequest<SeedRequest.Type, Seed> {

    public enum Type {
        PlantSeed,
        GetSeeds,
        KillPlant
    }

    public SeedRequest(Type type, List<Seed> payload) {
        this(type, payload, 1);
    }

    public SeedRequest(Type type, List<Seed> payload, Integer version) {
        super(version);
    }

}
