package com.spdb.fdev.fdemand;

import com.spdb.fdev.fdemand.base.utils.GitUtilsInit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@Import({GitUtilsInit.class})
public class FdevDemandApplication {

	public static void main(String[] args) {
		SpringApplication.run(FdevDemandApplication.class, args);
	}

}
