/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.error;

public class RestError extends GeneralError {

    public String url;

    public RestError(String message, String status) {
        super(message, status);
    }

    public RestError(String message, Object errorObject, Integer errorCode) {
        super(message, errorObject, errorCode);
    }

    public RestError(String message, Integer errorCode) {
        super(message, null, errorCode);
    }

    public RestError(String message, String status, String url) {
        super(message, status);
        this.url = url;
    }
}
