/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package samples.vm.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "deviceType")
@JsonSubTypes({
      @JsonSubTypes.Type(value = VirtualDisk.class),
      @JsonSubTypes.Type(value = VirtualUSB.class)
})
public class VirtualDevice {

   @Getter
   @Setter
   private int key;

   @Getter @Setter
   private String deviceName;
}
