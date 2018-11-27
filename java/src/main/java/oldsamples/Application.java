package oldsamples;

/*
 * Copyright(c) VMware Inc. 2017-2018
 */

import com.vmware.bifrost.bridge.spring.config.annotation.EnableBifrost;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBifrost
@ComponentScan(basePackages = {
<<<<<<< Updated upstream:java/src/main/java/samples/Application.java
        "samples"
=======
        "com.vmware.bifrost.bridge.spring.config",
        "com.vmware.bifrost.bridge.spring.controllers",
        "com.vmware.bifrost.bridge.spring.handlers",
        "com.vmware.bifrost.bridge.spring",
        "com.vmware.bifrost.bridge",
        "com.vmware.bifrost.bus",
        "oldsamples"
>>>>>>> Stashed changes:java/src/main/java/oldsamples/Application.java
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
