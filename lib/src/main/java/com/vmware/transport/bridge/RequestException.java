package com.vmware.transport.bridge;

public class RequestException extends Exception {
    public RequestException(String message) {
        super(message);
    }

    public RequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
