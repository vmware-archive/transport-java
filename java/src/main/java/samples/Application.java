package samples;

/*
 * Copyright(c) VMware Inc. 2017-2018
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
