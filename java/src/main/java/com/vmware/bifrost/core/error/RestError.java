/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.error;

public class RestError extends GeneralError {

    public String url;

    public RestError(String message, String status) {
        super(message, status);
    }

    public RestError(String message, Object errorObject, String errorCode) {
        super(message, errorObject, errorCode);
    }

    public RestError(String message, String status, String url) {
        super(message, status);
        this.url = url;
    }
}
