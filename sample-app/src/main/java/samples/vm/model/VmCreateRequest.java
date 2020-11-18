/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
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
