package com.spdb.fdevconfigserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication(exclude= { SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@EnableConfigServer
public class FdevConfigserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(FdevConfigserverApplication.class, args);
	}

}