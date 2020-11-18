/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VirtualUSB extends VirtualDevice {

   @Getter @Setter
   private boolean connected;

   @Getter @Setter
   private UsbSpeed[] speed;

   public static VirtualUSB newInstance(int key, boolean connected, UsbSpeed... speed) {
      VirtualUSB usb = new VirtualUSB();
      usb.setKey(key);
      usb.setConnected(connected);
      usb.setSpeed(speed);
      usb.setDeviceName("VirtualUSB-" + key);
      return usb;
   }

   public enum UsbSpeed {
      low,
      full,
      high,
      superSpeed,
      superSpeedPlus
   }
}
