/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class VmPowerOperationResponse extends BaseVmResponse {

   @Getter
   Map<VmRef, Boolean> operationResults = new HashMap<>();
}
