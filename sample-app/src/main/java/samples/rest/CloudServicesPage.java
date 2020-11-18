/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.rest;

import lombok.Getter;
import lombok.Setter;

public class CloudServicesPage {

    @Getter @Setter
    private String id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String url;

    @Getter @Setter
    private String time_zone;

    @Getter @Setter
    private String updated_at;
}