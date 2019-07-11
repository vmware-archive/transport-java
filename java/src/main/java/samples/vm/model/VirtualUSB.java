/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
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
