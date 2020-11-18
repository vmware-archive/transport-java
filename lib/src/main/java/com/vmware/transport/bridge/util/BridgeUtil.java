/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package com.vmware.transport.bridge.util;

import com.vmware.transport.bridge.BridgeChannelMode;
import com.vmware.transport.bus.EventBus;

public class BridgeUtil {

   private final static String BRIDGE_CHANNEL_MODE_ATTR = "BRIDGE_CHANNEL_MODE";

   private BridgeUtil() {}

   public static boolean setBridgeChannelMode(
         EventBus eventBus, String channelName, BridgeChannelMode bridgeChannelMode) {

      // Ensure that a Channel object exists as we cannot set attributes to
      // non-existing channels.
      eventBus.getApi().getChannelObject(channelName, BridgeUtil.class.getSimpleName());
      // Store the channel mode as a channel attribute.
      return eventBus.getApi().setChannelAttribute(
            channelName, BRIDGE_CHANNEL_MODE_ATTR, bridgeChannelMode);
   }

   public static BridgeChannelMode getBridgeChannelMode(
         EventBus eventBus, String channelName) {

      Object channelTypeAttrValue = eventBus.getApi().getChannelAttribute(
            channelName, BRIDGE_CHANNEL_MODE_ATTR);
      if (channelTypeAttrValue != null) {
         return (BridgeChannelMode) channelTypeAttrValue;
      }
      // If the attribute is not set, return the default value.
      return BridgeChannelMode.REQUESTS_AND_RESPONSES;
   }
}
