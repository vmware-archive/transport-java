package com.vmware.bifrost.bridge;

import java.util.Date;
import java.util.UUID;

public abstract class AbstractFrame<ReqP> {

    protected UUID id;
    protected Date created;
    protected Integer version;
    protected ReqP payload;

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

    public AbstractFrame(Integer version, UUID uuid, ReqP payload) {
        this.id = uuid;
        this.created = new Date();
        this.version = version;
        this.payload = payload;
    }

    public UUID getId() {
        return id;
    }

    public ReqP getPayload() {
        return this.payload;
    }

    public void setPayload(ReqP payload) {
        this.payload = payload;
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
