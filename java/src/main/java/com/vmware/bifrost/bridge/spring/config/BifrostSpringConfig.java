/*
 * Copyright 2018 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package com.vmware.bifrost.bridge.spring.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Loads Bifrost message bus and bridge components.
 */
@Configuration
@ComponentScan(basePackages = {
      "com.vmware.bifrost.bridge.spring.config",
      "com.vmware.bifrost.bridge.spring.controllers",
      "com.vmware.bifrost.bridge.spring.handlers",
      "com.vmware.bifrost.bridge.spring",
      "com.vmware.bifrost.bridge",
      "com.vmware.bifrost.bus"
})
public class BifrostSpringConfig {
}
