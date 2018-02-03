package com.vmware.bifrost.bridge;

import java.util.Date;
import java.util.UUID;

public abstract class AbstractFrame {

    private UUID id;
    private Date created;
    private Integer version;

    public AbstractFrame() { }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public AbstractFrame(Integer version, UUID uuid) {
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
