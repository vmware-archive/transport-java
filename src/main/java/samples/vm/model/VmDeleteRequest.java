/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VmDeleteRequest extends BaseVmRequest {

   @Getter @Setter
   VmRef vm;
}
