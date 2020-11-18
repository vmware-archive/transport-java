/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.broker;

public class TestGalacticChannelConfig extends GalacticChannelConfig {

   public String remoteChannel;

   public TestGalacticChannelConfig(String brokerId, String remoteChannel) {
      super(brokerId);
      this.remoteChannel = remoteChannel;
   }
}
