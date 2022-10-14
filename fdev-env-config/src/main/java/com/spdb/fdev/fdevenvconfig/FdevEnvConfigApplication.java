package com.spdb.fdev.fdevenvconfig;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * @author xxx
 * @date 2019/7/5 13:06
 */
@EnableAsync
@ComponentScan(basePackages = {"com.spdb.fdev"})
@SpringBootApplication
public class FdevEnvConfigApplication {

    public static void main(String[] args) {
        SpringApplication.run(FdevEnvConfigApplication.class, args);
    }
}
