package com.vmware.bifrost.bridge.spring;

/*
 * Copyright(c) VMware Inc. 2017
 */

import com.vmware.bifrost.bus.MessagebusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.vmware.bifrost.bridge.spring.config",
        "com.vmware.bifrost.bridge.spring.controllers",
        "com.vmware.bifrost.bridge.spring.handlers",
        "com.vmware.bifrost.bridge.spring",
        "com.vmware.bifrost.bridge",
        "com.vmware.bifrost.bus",
        "samples"
})
public class Application {

    @Autowired
    private MessagebusService bus;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }




    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("***** STARTING APP *******");
            this.bus.init();


        };
    }

}