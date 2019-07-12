/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VirtualDisk extends VirtualDevice {

   @Getter
   @Setter
   private long capacityMB;

   @Getter @Setter
   private DiskFormat diskFormat;

   public static VirtualDisk newInstance(int key, long capacityMB, DiskFormat diskFormat) {
      VirtualDisk virtualDisk = new VirtualDisk();
      virtualDisk.setKey(key);
      virtualDisk.setDiskFormat(diskFormat);
      virtualDisk.setCapacityMB(capacityMB);
      virtualDisk.setDeviceName("VirtualDisk-" + key);
      return virtualDisk;
   }

   public enum DiskFormat {
      native_512,
      emulated_512,
      native_4k
   }
}
