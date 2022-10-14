package com.fdev.database.spdb.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * ScheduleTasks
 *
 * @blame Android Team
 */
@Component
@RefreshScope
public class ScheduleTasks {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleTasks.class);


    @Value("${schedule.flag}")
    private boolean TaskFlag;


    /**
     * 定时启动
     * 5秒执行一次代码
     *  corn表达式
     */
    @Scheduled(cron = "5 * * * * ?")
    //@Scheduled(cron = "0 35 10 * * ?")  
    public void scheduleTask() {

        if (TaskFlag) {
            logger.info(">>>>>>>>>>>>>>>>>执行开始");
            try {
//                fileMsgListener.FileListener();
            } catch (Exception e) {
                logger.error(">>>>>>>>>>>>>>>>>执行失败");
            }
            logger.info(">>>>>>>>>>>>>>>>>执行结束");
        }
    }



}
