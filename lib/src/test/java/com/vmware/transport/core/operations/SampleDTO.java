/*
 * Copyright 2018-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.core.operations;

import lombok.Getter;
import lombok.Setter;

public class SampleDTO {

    @Getter @Setter
    public String name;

    @Getter @Setter
    public Number value;

}
