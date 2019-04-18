/**
 * Copyright(c) VMware Inc. 2019
 */
package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

public class TokenValidationResults {
    private boolean isValid;
    private String errorMessage;

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
