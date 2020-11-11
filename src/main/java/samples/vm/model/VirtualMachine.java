/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package samples.vm.model;

import lombok.Getter;
import lombok.Setter;

public class VirtualMachine {

   @Getter @Setter
   private VmRef vmRef;

   @Getter @Setter
   private String name;

   @Getter @Setter
   private VirtualHardware hardware;

   @Getter @Setter
   private RuntimeInfo runtimeInfo;
}
