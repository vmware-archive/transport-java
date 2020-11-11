package com.vmware.transport.bridge;

import java.util.Date;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractFrame<ReqP> {

    @Getter @Setter
    public UUID id;

    @Getter @Setter
    public Date created;

    @Getter @Setter
    public Integer version;

    @Getter @Setter
    public ReqP payload;

    @Getter @Setter
    public String message;

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
