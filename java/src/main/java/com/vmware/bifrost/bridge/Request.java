package com.vmware.bifrost.bridge;

import java.util.UUID;

public class Request<ReqP> extends AbstractFrame {

   // public ReqT type;
    public ReqP payload;
    public String type;

    public Request(Integer version, UUID uuid, String type, ReqP payload) {
        super(version, uuid);
        this.payload = payload;
        this.type = type;
    }


//    public ReqT getType() {
//        return this.type;
//    }

    public String getType() {
        return this.type;
    }

    public ReqP getPayload() {
        return this.payload;
    }

    public String toString() {
        return "Request ID: " + this.getId();
    }

}
