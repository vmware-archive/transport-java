/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
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
