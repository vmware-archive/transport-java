/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.rest;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

public class CloudServicesStatusResponse {

    @Getter @Setter
    private CloudServicesStatus status;

    @Getter @Setter
    private CloudServicesPage page;

}
