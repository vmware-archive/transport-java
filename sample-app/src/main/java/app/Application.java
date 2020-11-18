/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: BSD-2-Clause
 *
 */
package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.vmware.transport.bridge.spring.config.annotation.EnableTransport;

@SpringBootApplication(scanBasePackages = "samples")
@EnableTransport
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}