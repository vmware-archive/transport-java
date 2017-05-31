package com.vmware.bifrost.bus.model;


public class Message<T> {

    private MessageType type;
    private Class<T> payloadClass;
    private Object payload;
    private MessageSchema schema;


    public Message(MessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.payloadClass = (Class<T>) payload.getClass();
    }

    public Message(MessageType type, Object payload, MessageSchema schema) {
        this(type, payload);
        this.schema = schema;
    }

    public void setPayloadClass(Class<T> payloadClass) {
        this.payloadClass = payloadClass;
    }

    public MessageSchema getSchema() {
        return schema;
    }

    public void setSchema(MessageSchema schema) {
        this.schema = schema;
    }

    public Class<T> getPayloadClass() {
        return payloadClass;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public boolean isRequest() {
        return this.type == MessageType.MessageTypeRequest;
    }

    public boolean isResponse() {
        return this.type == MessageType.MessageTypeResponse;
    }

    public boolean isError() {
        return this.type == MessageType.MessageTypeError;
    }

}
