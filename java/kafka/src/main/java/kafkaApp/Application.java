/*
 * Copyright 2019 VMware, Inc. All rights reserved. -- VMware Confidential
 */
package kafkaApp;

import com.vmware.bifrost.bridge.spring.config.annotation.EnableBifrost;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBifrost
@ComponentScan(basePackages = {"kafkaSamples"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}