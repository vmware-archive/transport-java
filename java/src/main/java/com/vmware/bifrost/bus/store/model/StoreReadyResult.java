/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bus.store.model;

import io.reactivex.functions.Consumer;

public interface StoreReadyResult {
   void whenReady(Consumer<Void> successHandler);
}
