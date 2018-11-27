package samples;

/*
 * Copyright(c) VMware Inc. 2017-2018
 */

import com.vmware.bifrost.bridge.spring.config.annotation.EnableBifrost;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableBifrost
@ComponentScan(basePackages = {
        "samples",
        "com."
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}