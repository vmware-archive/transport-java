package com.vmware.bifrost.bridge;

import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractFrame<ReqP> {

    @Getter @Setter
    protected UUID id;

    @Getter @Setter
    protected Date created;

    @Getter @Setter
    protected Integer version;

    @Getter @Setter
    protected ReqP payload;

    @Getter @Setter
    protected String message;

    public AbstractFrame() { }

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

    public String toString() {
        return "Frame Request ID: " + this.getId();
    }
}
