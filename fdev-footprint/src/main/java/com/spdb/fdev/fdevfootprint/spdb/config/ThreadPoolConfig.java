package com.spdb.fdev.fdevfootprint.spdb.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.spdb.fdev.fdevfootprint.base.dict.Dict;

@Configuration
@EnableAutoConfiguration
public class ThreadPoolConfig {
	
	@Bean(name=Dict.KAFKA_PRODUCER_EXECUTOR)
	@ConfigurationProperties(prefix = "host.thread.pool.task")
	public ThreadPoolTaskExecutor getExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setThreadNamePrefix("KafkaProducer-Executor-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
		return executor;
	}
}
