/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public class VmRef {

   @Getter @Setter
   private String vcGuid;

   @Getter @Setter
   private String vmId;

   @Override
   public boolean equals(Object obj) {
      if (obj instanceof VmRef) {
         return Objects.equals(this.vcGuid, ((VmRef) obj).vcGuid) &&
               Objects.equals(this.vmId, ((VmRef) obj).vmId);
      }
      return false;
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.vcGuid, this.vmId);
   }

   @Override
   public String toString() {
      return this.vmId + ":" + this.vcGuid;
   }

   public static VmRef newInstance(String vmId, String vcGuid) {
      VmRef vmRef = new VmRef();
      vmRef.setVcGuid(vcGuid);
      vmRef.setVmId(vmId);
      return vmRef;
   }
}
