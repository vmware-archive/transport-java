/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VirtualHardware {

   @Getter @Setter
   private int memoryMB;

   @Getter @Setter
   private int numCPU;

   @Getter @Setter
   private VirtualDevice[] devices;

   public static VirtualHardware newInstance(int numCpu, int memoryMB, VirtualDevice... devices) {
      VirtualHardware hardware = new VirtualHardware();
      hardware.setNumCPU(numCpu);
      hardware.setMemoryMB(memoryMB);
      hardware.setDevices(devices);
      return hardware;
   }
}
