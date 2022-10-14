package com.fdev.database.spdb.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

/**
 * StartSchedule
 *
 * @blame Android Team
 */
@Component
public class StartSchedule {
	
	@Bean
	public TaskScheduler testSchedule() {
		
		// 创建线程池
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
		threadPoolTaskScheduler.setPoolSize(10);
		threadPoolTaskScheduler.setThreadNamePrefix("fdatabase-schedule");
		return threadPoolTaskScheduler;

	}

}
