/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VmCreateResponse extends BaseVmResponse {

   @Getter @Setter
   private VirtualMachine vm;
}
