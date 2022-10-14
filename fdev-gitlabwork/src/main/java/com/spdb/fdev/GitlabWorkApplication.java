package com.spdb.fdev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

/**
 * GitlabWorkApplication
 *
 * 主类
 *
 * @blame Android Team
 */
@SpringBootApplication
@EnableScheduling
public class GitlabWorkApplication {

    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

	public static void main(String[] args) {
		SpringApplication.run(GitlabWorkApplication.class, args);
	}

}
