package com.vmware.transport.bridge;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@SuppressWarnings("unchecked")
public class Request<ReqP> extends AbstractFrame {

    @Getter @Setter
    private String targetUser;

    @Getter @Setter
    private String request;

    @Getter @Setter
    private String channel;

    @Getter @Setter
    private Object headers;

    @Getter @Setter
    private Map<String, Object> sessionAttributes;

    public <T> T getSessionAttribute(String attrKey) {
        if (sessionAttributes == null) {
            return null;
        }
        return (T)sessionAttributes.get(attrKey);
    }

    @Getter @Setter
    private Boolean isRejected = false;

    public Request() {}

    public Request(Integer version, UUID id, String request, ReqP payload) {
        super(version, id, payload);
        this.request = request;
    }

    // We need to store the request channel because we need to respond there
    public Request(Integer version, UUID id, String request, ReqP payload, String channel) {
        this(version, id, request, payload);
        this.channel = channel;
    }

    public Request(UUID id, String request, ReqP payload) {
        this(1, id, request, payload);
    }

    public Request(String request, ReqP payload) {
        this(1, UUID.randomUUID(), request, payload);
    }

    public Request(String request) {
        this(1, UUID.randomUUID(), request, null);
    }

    public Request(UUID id, String request) {
        this(1, id, request, null);
    }

    public String toString() {
        return "Request ID: " + this.getId();
    }

}
