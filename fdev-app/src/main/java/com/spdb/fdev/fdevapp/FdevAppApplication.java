package com.spdb.fdev.fdevapp;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(basePackages = {"com.spdb.fdev"})
@SpringBootApplication
@EnableAsync
public class FdevAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FdevAppApplication.class, args);
	}


}

