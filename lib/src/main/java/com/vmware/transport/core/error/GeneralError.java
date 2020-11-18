/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.error;

public class GeneralError {
    public Object errorObject;
    public Integer errorCode;
    public String message;
    public String status;

    public GeneralError() {}

    public GeneralError(String message, String status) {
        this.status = status;
        this.message = message;
    }

    public GeneralError(String message, Object errorObject, Integer errorCode) {
        this.errorCode = errorCode;
        this.message = message;
        this.errorObject = errorObject;
    }
}
