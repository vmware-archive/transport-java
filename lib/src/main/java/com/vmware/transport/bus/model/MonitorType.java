/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.model;

public enum MonitorType {
    MonitorCloseChannel,
    MonitorCompleteChannel,
    MonitorDestroyChannel,
    MonitorNewChannel,
    MonitorData,
    MonitorError,
    MonitorDropped,
    MonitorNewBridgeSubscription,
    MonitorNewGalacticChannel
}
