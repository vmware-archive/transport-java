/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.broker;

public class TestGalacticSubscription extends MessageBrokerSubscription {

   public String subscriptionId;

   public GalacticMessageHandler callback;

   public TestGalacticSubscription(String id, GalacticMessageHandler callback) {
      this.subscriptionId = id;
      this.callback = callback;
   }
}
