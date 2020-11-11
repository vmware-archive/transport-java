/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
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
