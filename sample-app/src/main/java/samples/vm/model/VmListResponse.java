/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VmListResponse extends BaseVmResponse {

   @Getter @Setter
   private VirtualMachine virtualMachines[];
}
