package com.spdb.fdev.codeReview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableAsync
@ComponentScan("com.spdb.fdev")
public class ProcessToolApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProcessToolApplication.class, args);
    }

}


