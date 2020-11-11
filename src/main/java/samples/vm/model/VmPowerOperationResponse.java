/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class VmPowerOperationResponse extends BaseVmResponse {

   @Getter @Setter
   VmPowerOperationResponseItem[] opResults;
}
