package com.vmware.transport.core.autogen;

import lombok.Getter;

public class ServiceVersion {
    @Getter
    private String name;

    @Getter
    private String version;

    public ServiceVersion(String serviceName, String serviceVersion) {
        this.name = serviceName;
        this.version = serviceVersion;
    }

    public boolean isValid() {
        return this.name != null && this.version != null;
    }
}
