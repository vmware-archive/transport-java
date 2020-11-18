/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VmPowerOperationRequest extends BaseVmRequest {

   @Getter @Setter
   private VmRef vmRefs[];

   @Getter @Setter
   private PowerOperation powerOperation;

   public enum PowerOperation {
      powerOn,
      powerOff,
      reset,
      suspend
   }
}
