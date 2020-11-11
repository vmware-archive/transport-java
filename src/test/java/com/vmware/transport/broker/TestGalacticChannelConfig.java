/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.transport.broker;

public class TestGalacticChannelConfig extends GalacticChannelConfig {

   public String remoteChannel;

   public TestGalacticChannelConfig(String brokerId, String remoteChannel) {
      super(brokerId);
      this.remoteChannel = remoteChannel;
   }
}
