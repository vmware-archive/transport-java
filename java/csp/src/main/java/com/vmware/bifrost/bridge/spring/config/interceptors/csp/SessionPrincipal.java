/**
 * Copyright(c) VMware Inc. 2019
 */
package com.vmware.bifrost.bridge.spring.config.interceptors.csp;

import java.security.Principal;
import java.util.Objects;

public class SessionPrincipal implements Principal {
    private String name;

    public SessionPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Principal)) {
            return false;
        }
        Principal principal = (Principal) obj;
        return principal.getName() == this.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
