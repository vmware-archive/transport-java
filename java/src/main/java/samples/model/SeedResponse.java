package samples.model;

import com.vmware.bifrost.bridge.Response;
import io.swagger.client.model.Seed;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SeedResponse extends Response<Seed> {

    public SeedResponse(Integer version, UUID uuid, Date created, List<Seed> payload) {
        super(version, uuid, created, payload);
    }

    public SeedResponse(Integer version, List<Seed> payload) {
        this(version, UUID.randomUUID(), new Date(), payload);
    }

    public SeedResponse(UUID id, List<Seed> payload) {
        this(1, id, new Date(), payload);
    }

    public SeedResponse(List<Seed> payload) {
        this(1, UUID.randomUUID(), new Date(), payload);
    }

    public SeedResponse(UUID id, boolean error) {
        this(1, id, new Date(),null);
        this.error = true;
    }

    @Override
    public String toString() {
        return "Seed Response: (" + this.getId() + ")";
    }
}

