/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class RuntimeInfo {

   @Getter @Setter
   private String host;

   @Getter @Setter
   private PowerState powerState;

   public enum PowerState {
      poweredOff,
      poweredOn,
      suspended
   }
}
