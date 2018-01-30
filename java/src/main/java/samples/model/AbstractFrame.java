package samples.model;

import io.swagger.client.model.Seed;

import java.util.Date;
import java.util.UUID;

public abstract class AbstractFrame {

    protected UUID uuid;
    protected Date created;
    protected Integer version;

    public AbstractFrame(Integer version, UUID uuid) {
        this.uuid = uuid;
        this.created = new Date();
        this.version = version;
    }


    public AbstractFrame(Integer version) {
       this(version, UUID.randomUUID());
    }

    public UUID getUuid() {
        return uuid;
    }

    public Date getCreated() {
        return created;
    }

    public Integer getVersion() {
        return version;
    }
}
