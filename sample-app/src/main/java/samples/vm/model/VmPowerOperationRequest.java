/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
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
