/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import java.util.UUID;

public class MonitorChannel {
    public static String stream = "#" + UUID.randomUUID().toString().replaceAll("-", "");
}
