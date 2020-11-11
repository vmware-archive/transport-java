/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.model;

import java.util.UUID;

public class SystemChannels {

    public static String EXTERNAL_MESSAGE_BROKER =
          "#external-msg-broker-" + UUID.randomUUID().toString().replaceAll("-", "");

    private SystemChannels() {}
}
