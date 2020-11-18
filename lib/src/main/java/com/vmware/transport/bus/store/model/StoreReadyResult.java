/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bus.store.model;

import io.reactivex.functions.Consumer;

public interface StoreReadyResult {
   void whenReady(Consumer<Void> successHandler);
}
