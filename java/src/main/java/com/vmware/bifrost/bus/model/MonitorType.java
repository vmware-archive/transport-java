package com.vmware.bifrost.bus.model;

/**
 * Copyright(c) VMware Inc. 2017
 */
public enum MonitorType {
    MonitorCloseChannel,
    MonitorCompleteChannel,
    MonitorDestroyChannel,
    MonitorNewChannel,
    MonitorData,
    MonitorError,
    MonitorDropped
}
