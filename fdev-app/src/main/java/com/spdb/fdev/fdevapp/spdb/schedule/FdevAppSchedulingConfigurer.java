package com.spdb.fdev.fdevapp.spdb.schedule;

import com.spdb.fdev.fdevapp.spdb.schedule.service.ClearPipelineScheduleTasks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RefreshScope
public class FdevAppSchedulingConfigurer implements SchedulingConfigurer {

    private Logger logger = LoggerFactory.getLogger(FdevAppSchedulingConfigurer.class);

    @Value("${clear.pipeline.cron}")
    private String clearPipelineCron;
    @Autowired
    private ClearPipelineScheduleTasks clearPipeline;


    /**
     * 定时任务配置
     *
     * @param taskRegistrar
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addCronTask(clearPipeline, clearPipelineCron);
    }

}
