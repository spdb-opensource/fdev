package com.spdb.fdev.fdevfootprint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ComponentScan(basePackages = {"com.spdb.fdev"})
@EnableScheduling
@SpringBootApplication
public class FdevFootPrintApplication {
	
    public static void main(String[] args) {
        SpringApplication.run(FdevFootPrintApplication.class, args);
    }
}
