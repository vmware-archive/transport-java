/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class BaseVmResponse {

   @Getter @Setter
   private boolean error;

   @Getter @Setter
   private String errorMessage;
}
