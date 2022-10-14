package com.spdb.common.aop.log;

import com.spdb.common.dict.Dict;
import com.spdb.common.util.CronUtil;
import com.spdb.common.util.DateUtil;
import com.spdb.common.util.Util;
import com.spdb.job.service.IExecutionPlanService;
import com.spdb.common.config.database.MultiJdbcAccess;
import com.spdb.job.service.IPausedStrategyManagerService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lizz
 */
@Aspect
@Configuration
public class JobAddInstanceAspect {
    @Autowired
    MultiJdbcAccess multiJdbcAccess;

    @Autowired
    Scheduler scheduler;

    @Autowired
    IExecutionPlanService executionPlanService;
    @Autowired
    IPausedStrategyManagerService pausedStrategyManagerService;

    @Pointcut("execution(* com.spdb.job.service.impl.TaskManagerServiceImpl.addJob(..))")
    public void execute() {
    }

    @Around("execute()")
    public void execute(ProceedingJoinPoint point) throws Throwable {
        point.proceed();
        Object[] args = point.getArgs();
        Map<String, Object> requestMap = (Map<String, Object>) args[0];
        if (Dict.CRON.equals(requestMap.get(Dict.SCHEDULERTYPE))) {
            String cronExpression = (String) requestMap.get(Dict.CRONEXPRESSION);
            List<Long> cronList = CronUtil.getNextExecTimeBetween(cronExpression, null, new Date(), DateUtil.getTodayEndTime());
            for (Long schedTime : cronList) {
                requestMap.put(Dict.SCHEDTIME, schedTime);
                String triggerName = requestMap.get(Dict.SCHEDULERTYPE) + (String) requestMap.get(Dict.JOBNAME);
                requestMap.put(Dict.TRIGGERNAME, triggerName);
                requestMap.put(Dict.TRIGGERSTATE, Dict.JOB_STATE_WAITING);
                int rows = executionPlanService.insertJobInfo(requestMap);
                if (rows > 0) {
                    Map pausedTriggerMap = pausedStrategyManagerService.selectPausedTriggerBetweenDate(requestMap);
                    List<Map<String, Object>> rowList = (List<Map<String, Object>>) pausedTriggerMap.get("rows");
                    if (Util.isNullOrEmpty(rowList)) {
                        continue;
                    }
                    for (Map map : rowList) {
                        Map pausedTriggerDateJobMap = new HashMap();
                        pausedTriggerDateJobMap.put(Dict.SCHEDNAME, scheduler.getSchedulerName());
                        pausedTriggerDateJobMap.put(Dict.PAUSEDDATEID, map.get(Dict.ID));
                        pausedTriggerDateJobMap.put(Dict.TRIGGERGROUP, map.get(Dict.JOBGROUP));
                        pausedTriggerDateJobMap.put(Dict.TRIGGERNAME, triggerName);
                        pausedTriggerDateJobMap.put(Dict.JOBGROUP, map.get(Dict.JOBGROUP));
                        pausedTriggerDateJobMap.put(Dict.JOBNAME, requestMap.get(Dict.JOBNAME));
                        multiJdbcAccess.getSqlMap().insert("PausedStrategyManager.insertpausedTriggerDateJob", pausedTriggerDateJobMap);
                    }
                }
                break;
            }
        }
    }
}