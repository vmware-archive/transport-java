package samples.model;

import io.swagger.client.model.Seed;

import java.util.List;
import java.util.UUID;

public class SeedResponse extends AbstractResponse<Seed> {


    public SeedResponse(UUID id, boolean error) {
        super(id, error, 1);
    }

    public SeedResponse(List<Seed> payload) {
        this(payload, 1);
    }

    public SeedResponse(List<Seed> payload, Integer version) {
        super(UUID.randomUUID(), payload, version);
    }


    public SeedResponse(UUID uuid, List<Seed> payload) {
        super(uuid, payload, 1);
    }

    public SeedResponse(UUID uuid, List<Seed> payload, Integer version) {
        super(uuid, payload, version);
    }

}

