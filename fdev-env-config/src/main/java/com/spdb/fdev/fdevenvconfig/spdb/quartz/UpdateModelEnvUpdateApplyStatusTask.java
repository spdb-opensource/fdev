package com.spdb.fdev.fdevenvconfig.spdb.quartz;

import com.spdb.fdev.fdevenvconfig.base.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

/**
 * 定时任务，超过约定时间未核对的申请，改为超期overtime
 */
@Component
@EnableScheduling
@RefreshScope
public class UpdateModelEnvUpdateApplyStatusTask implements SchedulingConfigurer {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${update.modelenvupdateapply.status.cron}")
    private String updateModelenvupdateapplyStatusCorn;
    @Autowired
    private UpdateModelEnvUpdateApplyStatusCallable updateModelEnvUpdateApplyStatusCallable;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        logger.info("update time : {}", DateUtil.getCurrentDate(DateUtil.DATETIME_ISO_FORMAT));
        taskRegistrar.addCronTask(updateModelEnvUpdateApplyStatusCallable, updateModelenvupdateapplyStatusCorn);
    }

}
