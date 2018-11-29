/**
 * Copyright(c) VMware Inc. 2018
 */
package com.vmware.bifrost.core.error;

public class GeneralError {
    public Object errorObject;
    public String errorCode;
    public String message;
    public String status;

    public GeneralError(String message, String status) {
        this.status = status;
        this.message = message;
    }

    public GeneralError(String message, Object errorObject, String errorCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.errorObject = errorObject;
    }
}
