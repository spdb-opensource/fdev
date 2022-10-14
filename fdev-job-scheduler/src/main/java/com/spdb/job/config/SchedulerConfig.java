package com.spdb.job.config;

//import com.csii.pe.log.monitor.JMXLogMonitor;
import com.spdb.common.globalseqno.SnowflakeIdWorker;
import com.spdb.quartz.MyQuartzJobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;


/**
 * @author lizz
 */
@Configuration
@RefreshScope
public class SchedulerConfig {

//    @Value("${JMX.monitor.interval.seconds}")
//    private long interval = 10;

    @Autowired
    MyQuartzJobFactory myQuartzJobFactory;
    @Autowired
    QuartzProperties quartzProperties;
    @Value("${spdb.scheduler.name}")
    private String schedulerName;

    @Bean
    public SnowflakeIdWorker snowflakeIdWorker() {
        return new SnowflakeIdWorker(0, 0);
    }

    /**
     * 任务调度器
     *
     * @param dataSource
     * @return
     */
    @Bean
    public SchedulerFactoryBean quartzScheduler(@Qualifier("schedulerDataSource") DataSource dataSource) {
        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setSchedulerName(schedulerName);
        schedulerFactoryBean.setDataSource(dataSource);
        // 自动启动
        schedulerFactoryBean.setAutoStartup(true);
        // 延时启动，应用启动成功后在启动
        schedulerFactoryBean.setStartupDelay(30);
        schedulerFactoryBean.setJobFactory(myQuartzJobFactory);
        // 覆盖DB中JOB：true、以数据库中已经存在的为准：false
        schedulerFactoryBean.setOverwriteExistingJobs(true);
        schedulerFactoryBean.setApplicationContextSchedulerContextKey("applicationContext");
        if (!this.quartzProperties.getProperties().isEmpty()) {
            schedulerFactoryBean.setQuartzProperties(this.asProperties(this.quartzProperties.getProperties()));
        }
        return schedulerFactoryBean;
    }

    private Properties asProperties(Map<String, String> source) {
        Properties properties = new Properties();
        properties.putAll(source);
        return properties;
    }

//    @Bean(initMethod = "init", destroyMethod = "shutdown")
//    public JMXLogMonitor jmxLogMonitor() {
//        JMXLogMonitor jmxLogMonitor = new JMXLogMonitor();
//        jmxLogMonitor.setInterval(interval);
//        return jmxLogMonitor;
//    }
}