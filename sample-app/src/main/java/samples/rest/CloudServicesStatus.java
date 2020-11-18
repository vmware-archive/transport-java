/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.rest;

import lombok.Getter;
import lombok.Setter;

public class CloudServicesStatus {

    @Getter @Setter
    private String indicator;

    @Getter @Setter
    private String description;

}