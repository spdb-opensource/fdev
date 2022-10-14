package com.spdb.fdev.spdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@ComponentScan(basePackages = {"com.spdb.fdev"})
@SpringBootApplication
@EnableScheduling
public class FdevBlueKingApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdevBlueKingApplication.class, args);
    }

}
