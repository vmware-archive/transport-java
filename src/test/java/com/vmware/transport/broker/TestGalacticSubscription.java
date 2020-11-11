/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
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
