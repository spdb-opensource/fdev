package com.spdb.fdev.fdevinterface;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(basePackages = {"com.spdb.fdev"})
@EnableAsync
@SpringBootApplication
public class FdevInterfaceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdevInterfaceApplication.class, args);
    }
}
