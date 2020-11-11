package com.vmware.transport.bus.model;

import java.util.UUID;

/*
 * Copyright(c) VMware Inc. 2017
 */
public class MonitorChannel {
    public static String stream = "#" + UUID.randomUUID().toString().replaceAll("-", "");
}
