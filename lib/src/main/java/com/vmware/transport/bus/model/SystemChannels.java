/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

import java.util.UUID;

public class SystemChannels {

    public static String EXTERNAL_MESSAGE_BROKER =
          "#external-msg-broker-" + UUID.randomUUID().toString().replaceAll("-", "");

    private SystemChannels() {}
}
