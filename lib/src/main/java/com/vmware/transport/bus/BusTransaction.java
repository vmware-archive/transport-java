/*
 * Copyright 2017-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus;

public interface BusTransaction<T> {
    void unsubscribe();
    boolean isSubscribed();
    void tick(T payload);
    void error(T payload);
}
