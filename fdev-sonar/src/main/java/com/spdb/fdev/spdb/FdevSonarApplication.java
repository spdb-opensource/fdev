package com.spdb.fdev.spdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@ComponentScan(basePackages = {"com.spdb.fdev"})
@SpringBootApplication
public class FdevSonarApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdevSonarApplication.class, args);
    }

}
