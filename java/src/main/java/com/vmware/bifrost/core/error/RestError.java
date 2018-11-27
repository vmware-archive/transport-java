/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.error;

public class RestError extends GeneralError {

    public String url;

    RestError(String message, String status) {
        super(message, status);
    }

    RestError(String message, Object errorObject, String errorCode) {
        super(message, errorObject, errorCode);
    }

    RestError(String message, String status, String url) {
        super(message, status);
        this.url = url;
    }
}
