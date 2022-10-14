package com.spdb.fdev.fuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com.spdb.fdev"})
@SpringBootApplication
public class FdevUserApplication{

	public static void main (String[] args)
	{
		SpringApplication.run(FdevUserApplication.class, args);
	}
}

//