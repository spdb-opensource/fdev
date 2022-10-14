package com.spdb.common.schedule;

import com.spdb.common.exception.CustomException;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import java.util.Date;
import java.util.Set;

/**
 * 自定义调度器
 *
 * @author lizz
 */

@Configuration
public class JobDynamicScheduler {

    private static Scheduler scheduler;

    /**
     * 获取调度器
     *
     * @return
     */
    @Bean
    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) {
        scheduler = schedulerFactoryBean.getScheduler();
        return scheduler;
    }

    public static void start() {
        try {
            scheduler.start();
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static Date scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            return scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            //throw new CustomException(e.getMessage());
            throw new CustomException("任务不能重复添加");
        }
    }

    public static void scheduleJob(JobDetail jobDetail, Set<? extends Trigger> triggersForJob, boolean replace) {
        try {
            scheduler.scheduleJob(jobDetail, triggersForJob, replace);
        } catch (SchedulerException e) {
            //throw new CustomException(e.getMessage());
            throw new CustomException("任务不能重复添加");
        }
    }

    public static Date rescheduleJob(TriggerKey triggerKey, Trigger newTrigger) {
        try {
            return scheduler.rescheduleJob(triggerKey, newTrigger);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static boolean unscheduleJob(TriggerKey triggerKey) {
        try {
            return scheduler.unscheduleJob(triggerKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static boolean deleteJob(JobKey jobKey) {
        try {
            return scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static void pauseJob(JobKey jobKey) {
        try {
            scheduler.pauseJob(jobKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static void pauseTrigger(TriggerKey triggerKey) {
        try {
            scheduler.pauseTrigger(triggerKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static void resumeJob(JobKey jobKey) {
        try {
            scheduler.resumeJob(jobKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static void resumeTrigger(TriggerKey triggerKey) {
        try {
            scheduler.resumeTrigger(triggerKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static String getSchedulerName() {
        try {
            return scheduler.getSchedulerName();
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static String getSchedulerInstanceId() {
        try {
            return scheduler.getSchedulerInstanceId();
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static JobDetail getJobDetail(JobKey jobKey) {
        try {
            return scheduler.getJobDetail(jobKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static Trigger getTrigger(TriggerKey triggerKey) {
        try {
            return scheduler.getTrigger(triggerKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static boolean checkExists(JobKey jobKey) {
        try {
            return scheduler.checkExists(jobKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }

    public static boolean checkExists(TriggerKey triggerKey) {
        try {
            return scheduler.checkExists(triggerKey);
        } catch (SchedulerException e) {
            throw new CustomException(e.getMessage());
        }
    }
}
