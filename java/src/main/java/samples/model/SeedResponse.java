package samples.model;

import io.swagger.client.model.Seed;

import java.util.List;
import java.util.UUID;

public class SeedResponse extends AbstractResponse<io.swagger.client.model.Seed> {

    private List<Seed> seeds;

    public SeedResponse(List<Seed> payload) {
        this(payload, 1);
    }

    public SeedResponse(List<Seed> payload, Integer version) {
        super(version);
    }


    public SeedResponse(UUID uuid, List<Seed> payload) {
        super(uuid, null, 1);
    }

    public SeedResponse(UUID uuid, List<Seed> payload, Integer version) {
        super(uuid, payload, version);
    }

}

