package com.fdev.database;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FdevDatabaseApplication {

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(FdevDatabaseApplication.class, args);
	}

}
