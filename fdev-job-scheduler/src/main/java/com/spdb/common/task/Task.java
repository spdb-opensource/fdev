package com.spdb.common.task;

import com.spdb.common.dict.Dict;
import com.spdb.common.util.DateUtil;
import com.spdb.job.service.IExecutionPlanService;
import com.spdb.job.service.IPausedStrategyManagerService;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 刷新执行计划列表
 * @author lizz
 */
@Component
public class Task {
    private static final Logger logger = LoggerFactory.getLogger(Task.class);

    @Autowired
    IExecutionPlanService executionPlanService;

    @Autowired
    IPausedStrategyManagerService pausedStrategyManagerService;

    @Scheduled(cron = "0 */5 * * * *")
    public void generateFutureJobList() throws SchedulerException {
        executionPlanService.generateFutureJobList();
    }


    @Scheduled(cron = "0 8 1 * * *")
    public void deletePausedStrategy() throws SchedulerException {
        logger.info("Clean Paused Strategy Start");
        Map<String,Object> requestMap = new HashMap<>();
        LocalDateTime localDateTime = LocalDateTime.now();
        localDateTime = localDateTime.minusMonths(1);
        long delDateTime = DateUtil.asDate(localDateTime).getTime();
        requestMap.put("endPausedDate",delDateTime);
        List<Map<String, Object>> pausedJobList = pausedStrategyManagerService.selectPausedStrategy(requestMap);
        for (Map map : pausedJobList){
            Map<String, Object> deleteMap = new HashMap<>();
            deleteMap.put(Dict.ID, map.get(Dict.ID));
            pausedStrategyManagerService.deletePausedStrategy(deleteMap);
        }
        logger.info("Clean Paused Strategy End");
    }

}
