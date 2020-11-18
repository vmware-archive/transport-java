/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class VmPowerOperationResponseItem {

   @Getter @Setter
   VmRef vmRef;

   @Getter @Setter
   boolean operationResult;
}
