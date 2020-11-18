package app;

/*
 * Copyright(c) VMware Inc. 2017-2018
 */


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