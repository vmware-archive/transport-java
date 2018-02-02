package com.vmware.bifrost.bridge;

import java.util.Date;
import java.util.UUID;

public abstract class AbstractFrame {

    protected UUID id;
    protected Date created;
    protected Integer version;

    public AbstractFrame(Integer version, UUID uuid, Date created) {
        this.id = uuid;
        this.created = new Date();
        this.version = version;
    }
    
    public UUID getId() {
        return id;
    }

    public Date getCreated() {
        return created;
    }

    public Integer getVersion() {
        return version;
    }

    public String toString() {
        return "Frame Request ID: " + this.getId();
    }
}
