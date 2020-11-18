/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VmCreateRequest extends BaseVmRequest {

   @Getter @Setter
   private String name;

   @Getter @Setter
   private VirtualHardware virtualHardware;
}
