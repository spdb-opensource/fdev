package com.spdb.job.service.impl;

import com.spdb.common.dict.Dict;
import com.spdb.common.exception.CustomException;
import com.spdb.common.globalseqno.SnowflakeIdWorker;
import com.spdb.common.schedule.JobDynamicScheduler;
import com.spdb.common.util.CronUtil;
import com.spdb.common.util.DateUtil;
import com.spdb.common.util.IPUtils;
import com.spdb.common.util.Util;
import com.spdb.common.config.database.MultiJdbcAccess;
import com.spdb.job.service.IElasticSearchService;
import com.spdb.quartz.JobTemplate;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.spdb.job.service.IExecutionPlanService;
import com.spdb.job.service.ITaskManagerService;
import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * @author lizz
 */
@Service
public class ExecutionPlanServiceImpl implements IExecutionPlanService {
    private static final Logger logger = LoggerFactory.getLogger(ExecutionPlanServiceImpl.class);

    @Autowired
    private MultiJdbcAccess multiJdbcAccess;
    @Autowired
    private SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    ITaskManagerService taskManagerService;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private JobTemplate jobTemplate;
    @Autowired
    private IElasticSearchService elasticSearchService;

    @Override
    public Map<String, Object> selectJobInfo(Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        Object description = requestMap.get(Dict.DESCRIPTION);
        Object jobGroup = requestMap.get(Dict.JOBGROUP);
        Object jobState = requestMap.get(Dict.JOBSTATE);
        Long startTime = null;
        if(!Util.isNullOrEmpty(requestMap.get("startDate"))){
            startTime =  (long)requestMap.get("startDate");
        }
        Long endTime = null;;
        if(!Util.isNullOrEmpty(requestMap.get("endDate"))){
            endTime = (long)requestMap.get("endDate");
        }
        Object serialNum = requestMap.get(Dict.SERIALNUM); //调度流水号
        Map<String, Object> jobInfoMap = new HashMap<>();
        jobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobInfoMap.put(Dict.JOBSTATE, jobState);
        jobInfoMap.put(Dict.DESCRIPTION, description);
        jobInfoMap.put(Dict.JOBGROUP, jobGroup);
        jobInfoMap.put("startDateTime", startTime);
        jobInfoMap.put("endDateTime", endTime == null ? null : DateUtil.getDateEndTime(new Date(endTime)).getTime());
        jobInfoMap.put(Dict.SERIALNUM, serialNum);
        int pageNumber = (int) requestMap.get(Dict.PAGENUMBER);
        int pageSize = (int) requestMap.get(Dict.PAGESIZE);
        PageHelper.startPage(pageNumber, pageSize);
        List<Map<String, Object>> rows = multiJdbcAccess.getSqlMap().selectList("ExecutionPlan.selectJobOperationRecord", jobInfoMap);
        resultMap.put("rows", rows);
        Page<Map<String, Object>> listPage = (Page<Map<String, Object>>) rows;
        resultMap.put(Dict.TOTAL, listPage.getTotal());
        resultMap.put(Dict.PAGENUMBER, listPage.getPageNum());
        resultMap.put(Dict.PAGESIZE, listPage.getPageSize());
        resultMap.put(Dict.PAGES, listPage.getPages());
        return resultMap;
    }

    @Override
    public Map<String, Object> selectJobInfoOne(Map<String, Object> requestMap) {
        Map<String, Object> jobInfoMap = new HashMap<>();
        jobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobInfoMap.put(Dict.TRIGGERNAME, requestMap.get(Dict.TRIGGERNAME));
        jobInfoMap.put(Dict.TRIGGERGROUP, requestMap.get(Dict.TRIGGERGROUP));
        jobInfoMap.put(Dict.JOBSTATE, requestMap.get(Dict.JOBSTATE));
        jobInfoMap.put(Dict.SCHEDTIME, requestMap.get(Dict.SCHEDTIME));
        jobInfoMap.put(Dict.ID, requestMap.get(Dict.ID));
        return multiJdbcAccess.getSqlMap().selectOne("ExecutionPlan.selectJobOperationRecordOne", jobInfoMap);
    }

    @Override
    public Map<String, Object> selectExecutorPlanDetail(Map<String, Object> requestMap) {
        Object jobId = requestMap.get(Dict.JOBID);
        Map<String, Object> jobInfoMap = new HashMap<>();
        jobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobInfoMap.put(Dict.ID, jobId);
        return multiJdbcAccess.getSqlMap().selectOne("ExecutionPlan.selectExecutorPlanDetail", jobInfoMap);
    }

    @Override
    public int updateJobInfo(Map<String, Object> requestMap) {
        Object jobId = requestMap.get(Dict.JOBID);
        String jobState = (String) requestMap.get(Dict.JOBSTATE);
        Object firedTime = requestMap.get(Dict.FIREDTIME);
        Object startTime = requestMap.get(Dict.STARTTIME);
        Object endTime = requestMap.get(Dict.ENDTIME);
        Object identificationId = requestMap.get(Dict.IDENTIFICATIONID);
        Object acquiredTime = requestMap.get(Dict.ACQUIREDTIME);
        Object instanceName = requestMap.get(Dict.INSTANCENAME);
        //任务类型（0正常，1、立即执行，2取消的任务,3、延期任务，4、一次性任务）
        Object transType = requestMap.get(Dict.TRANSTYPE);
        String schedulerSeqNo = (String) requestMap.get(Dict.SCHEDULERSEQNO);
        String schedulerIp = IPUtils.getLocalIP();
        String executorIp = (String) requestMap.get(Dict.HOST);
        String schedulerResult = (String) requestMap.get(Dict.SCHEDULERRESULT);
        String executorHandler = (String) requestMap.get(Dict.EXECUTORHANDLER);
        Object executorParam = requestMap.get(Dict.EXECUTORPARAM);
        Object executorResult = requestMap.get(Dict.EXECUTORRESULT);
        Map<String, Object> jobInfoMap = new HashMap<>();
        jobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobInfoMap.put(Dict.ID, jobId);
        jobInfoMap.put(Dict.JOBSTATE, jobState);
        jobInfoMap.put(Dict.FIREDTIME, firedTime);
        jobInfoMap.put(Dict.STARTTIME, startTime);
        jobInfoMap.put(Dict.ENDTIME, endTime);
        jobInfoMap.put(Dict.IDENTIFICATIONID, identificationId);
        jobInfoMap.put(Dict.ACQUIREDTIME, acquiredTime);
        jobInfoMap.put(Dict.INSTANCENAME, instanceName);
        jobInfoMap.put(Dict.TRANSTYPE, transType);
        jobInfoMap.put(Dict.SCHEDULERSEQNO, schedulerSeqNo);
        jobInfoMap.put(Dict.SCHEDULERIP, schedulerIp);
        jobInfoMap.put(Dict.SCHEDULERRESULT, schedulerResult);
        jobInfoMap.put(Dict.EXECUTORHANDLER, executorHandler);
        jobInfoMap.put(Dict.EXECUTORPARAM, executorParam);
        jobInfoMap.put(Dict.EXECUTORIP, executorIp);
        jobInfoMap.put(Dict.EXECUTORRESULT, executorResult);
        return multiJdbcAccess.getSqlMap().update("ExecutionPlan.updateJobOperationRecord", jobInfoMap);
    }

    @Override
    public int insertJobInfo(Map<String, Object> requestMap) {
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        String triggerGroup = jobGroup;
        String instanceName = (String) requestMap.get(Dict.INSTANCENAME);
        Long schedTime = (Long) requestMap.get(Dict.SCHEDTIME);
        String triggerState = (String) requestMap.get(Dict.TRIGGERSTATE);
        String identificationId = (String) requestMap.get(Dict.IDENTIFICATIONID);
        Object acquiredtime = requestMap.get(Dict.ACQUIREDTIME);
        Object firedtime = requestMap.get(Dict.FIREDTIME);
        long createDate = System.currentTimeMillis();
        Map<String, Object> jobInfoMap = new HashMap<>();
        jobInfoMap.put(Dict.JOBNAME, jobName);
        jobInfoMap.put(Dict.JOBGROUP, jobGroup);
        jobInfoMap.put(Dict.TRIGGERNAME, triggerName);
        jobInfoMap.put(Dict.TRIGGERGROUP, triggerGroup);
        jobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobInfoMap.put(Dict.INSTANCENAME, instanceName);
        jobInfoMap.put(Dict.SCHEDTIME, schedTime);
        jobInfoMap.put(Dict.JOBSTATE, triggerState);
        jobInfoMap.put(Dict.CREATEDATE, createDate);
        jobInfoMap.put(Dict.IDENTIFICATIONID, identificationId);
        jobInfoMap.put(Dict.ACQUIREDTIME, acquiredtime);
        jobInfoMap.put(Dict.FIREDTIME, firedtime);
        return multiJdbcAccess.getSqlMap().insert("ExecutionPlan.insertJobOperationRecord", jobInfoMap);
    }

    @Override
    public int deleteExecutionPlan(Map<String, Object> requestMap) {
        requestMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        return multiJdbcAccess.getSqlMap().delete("ExecutionPlan.deleteExecutionPlan", requestMap);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public Map<String, Object> generateFutureJobList() {
        logger.info("Generate Future JobList Start");
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> lockMap = new HashMap<>();
        lockMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        lockMap.put(Dict.LOCKNAME, Dict.EXECUTION_ACCESS);
        multiJdbcAccess.getSqlMap().selectOne("ExecutionPlan.selectlockByLockName", lockMap);
        Map<String, Object> deleteJobInfoMap = new HashMap<>();
        deleteJobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        deleteJobInfoMap.put(Dict.JOBSTATE, Dict.JOB_STATE_WAITING);
        deleteJobInfoMap.put(Dict.STARTTIME, DateUtil.getTodayStartTime().getTime());
        deleteJobInfoMap.put(Dict.ENDTIME, DateUtil.getTodayEndTime().getTime());
        //任务类型（0正常，1、立即执行，2取消的任务,3、延期任务，4、一次性任务）
        deleteJobInfoMap.put(Dict.TRANSTYPE, 0);
        multiJdbcAccess.getSqlMap().delete("ExecutionPlan.deleteJobOperationRecord", deleteJobInfoMap);
        Map<String, Object> jobInfoMap = new HashMap<>();
        jobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobInfoMap.put(Dict.TRIGGERSTATE, Dict.JOB_STATE_WAITING);
        jobInfoMap.put(Dict.TRIGGERTYPE, Dict.JOB_TRIGGERTYPE_CRON);
        List<Map<String, Object>> rows = multiJdbcAccess.getSqlMap().selectList("ExecutionPlan.selectTriggerByCronTrigger", jobInfoMap);
        for (Map map : rows) {
            String cronExpression = (String) map.get(Dict.CRONEXPRESSION);
            List<Long> cronList =
                    CronUtil.getNextExecTimeBetween(cronExpression, null, new Date(), DateUtil.getTodayEndTime());
            for (Long schedTime : cronList) {
                Map<String, Object> selectMap = new HashMap<>();
                selectMap.put(Dict.JOBNAME, map.get(Dict.JOBNAME));
                selectMap.put(Dict.JOBGROUP, map.get(Dict.JOBGROUP));
                selectMap.put(Dict.TRIGGERNAME, map.get(Dict.TRIGGERNAME));
                selectMap.put(Dict.TRIGGERGROUP, map.get(Dict.TRIGGERGROUP));
                selectMap.put(Dict.SCHEDTIME, schedTime);
                selectMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
                List<Map<String, Object>> mapList = multiJdbcAccess.getSqlMap().selectList("ExecutionPlan.selectJobOperationRecordOne", selectMap);
                if (!Util.isNullOrEmpty(mapList)) {
                    continue;
                }
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put(Dict.JOBNAME, map.get(Dict.JOBNAME));
                requestMap.put(Dict.JOBGROUP, map.get(Dict.JOBGROUP));
                requestMap.put(Dict.TRIGGERNAME, map.get(Dict.TRIGGERNAME));
                requestMap.put(Dict.TRIGGERGROUP, map.get(Dict.TRIGGERGROUP));
                requestMap.put(Dict.SCHEDTIME, schedTime);
                requestMap.put(Dict.TRIGGERSTATE, map.get(Dict.TRIGGERSTATE));
                insertJobInfo(requestMap);
            }
        }
        logger.info("Generate Future JobList End");
        resultMap.put("rows", rows);
        return resultMap;
    }

    @Override
    public void cancelJob(Map<String, Object> requestMap) {
        Object id = requestMap.get(Dict.ID);
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(Dict.JOBID, id);
        //任务类型（0正常，1、立即执行，2取消的任务,3、延期任务，4、一次性任务）
        updateMap.put(Dict.TRANSTYPE, 2);
        updateMap.put(Dict.JOBSTATE, Dict.JOB_STATE_PAUSED);
        updateJobInfo(updateMap);
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        String triggerGroup = (String) requestMap.get(Dict.TRIGGERGROUP);
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        long schedTime = (long) requestMap.get(Dict.SCHEDTIME);
        TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
        Trigger trigger = JobDynamicScheduler.getTrigger(triggerKey);
        if (trigger instanceof CronTriggerImpl) {
            Map<String, Object> deleteMap = new HashMap<>();
            deleteMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
            deleteMap.put(Dict.NEXTFIRETIME, schedTime);
            deleteMap.put(Dict.TRIGGERGROUP, triggerGroup);
            deleteMap.put(Dict.TRIGGERNAME, triggerName);
            deleteMap.put(Dict.JOBGROUP, jobGroup);
            deleteMap.put(Dict.JOBNAME, jobName);
            multiJdbcAccess.getSqlMap().delete("ExecutionPlan.deleteSimpleTrigger",deleteMap);
            multiJdbcAccess.getSqlMap().delete("ExecutionPlan.deleteTrigger",deleteMap);
        }

    }

    @Override
    public void postponeJob(Map<String, Object> requestMap) {
        if (selectLessTimeJobNum(requestMap) > 0) {
            throw new CustomException("该任务不能延期");
        }

        Integer minutes = (Integer) requestMap.get(Dict.POSTPONETIME);
        Object id = requestMap.get(Dict.ID);
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        String triggerGroup = (String) requestMap.get(Dict.TRIGGERGROUP);
        long schedTimeNow = (long) requestMap.get(Dict.SCHEDTIME);
        Instant instant = Instant.ofEpochMilli(schedTimeNow).plus(Duration.ofMinutes(minutes));
        long schedTimeFuture = instant.toEpochMilli();

        TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
        Trigger trigger = JobDynamicScheduler.getTrigger(triggerKey);
        if (trigger instanceof CronTriggerImpl) {
            long nextFireTime = trigger.getFireTimeAfter(new Date(schedTimeNow)).getTime();
            if (nextFireTime - schedTimeFuture < 0) {
                throw new CustomException("延期时间必须小于下一次执行时间");
            }
        }

        if (schedTimeNow - System.currentTimeMillis() < 1000 * 60 * 5) {
            throw new CustomException("必须提前5min进行延期操作");
        }
        Map<String, Object> recordMap = new HashMap<>();
        recordMap.put(Dict.ID, id);
        recordMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        recordMap.put(Dict.SCHEDTIME, schedTimeFuture);
        //任务类型（0正常，1、立即执行，2取消的任务,3、延期任务，4、一次性任务）
        recordMap.put(Dict.TRANSTYPE, 3);
        multiJdbcAccess.getSqlMap().update("ExecutionPlan.updateJobOperationRecord", recordMap);
        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        triggerMap.put(Dict.NEXTFIRETIME, schedTimeFuture);
        triggerMap.put(Dict.JOBNAME, jobName);
        triggerMap.put(Dict.JOBGROUP, jobGroup);
        triggerMap.put(Dict.TRIGGERNAME, triggerName);
        triggerMap.put(Dict.TRIGGERGROUP, triggerGroup);
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put(Dict.TRIGGERNAME, triggerName);
        selectMap.put(Dict.TRIGGERGROUP, triggerGroup);
        selectMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        Map map = multiJdbcAccess.getSqlMap().selectOne("TaskManager.selectTrigger", selectMap);
        if ((Dict.JOB_TRIGGERTYPE_CRON.equals(map.get(Dict.TRANSTYPE)) && 2 != (int) map.get(Dict.MISFIREINSTR)) || Dict.JOB_TRIGGERTYPE_SIMPLE.equals(map.get(Dict.TRANSTYPE))) {
            triggerMap.put(Dict.JOBID, id);
        }
        multiJdbcAccess.getSqlMap().update("ExecutionPlan.updateTrigger", triggerMap);
    }

    @Override
    public int selectLessTimeJobNum(Map<String, Object> requestMap) {
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerGroup = (String) requestMap.get(Dict.TRIGGERGROUP);
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        Object schedTime = requestMap.get(Dict.SCHEDTIME);
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put(Dict.JOBNAME, jobName);
        selectMap.put(Dict.JOBGROUP, jobGroup);
        selectMap.put(Dict.TRIGGERGROUP, triggerGroup);
        selectMap.put(Dict.TRIGGERNAME, triggerName);
        selectMap.put("nowTime", System.currentTimeMillis());
        selectMap.put(Dict.SCHEDTIME, schedTime);
        selectMap.put(Dict.JOBSTATE, Dict.JOB_STATE_WAITING);
        selectMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        List<Map> rows = multiJdbcAccess.getSqlMap().selectList("ExecutionPlan.selectOperationRecordLessTimeByJobNum", selectMap);
        return rows.size();
    }

    @Override
    public void triggerJob(Map<String, Object> requestMap) {
        Object jobId = requestMap.get(Dict.ID);
        Map selectMap = new HashMap();
        selectMap.put(Dict.ID, jobId);
        selectMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        Map jobInfoMap = multiJdbcAccess.getSqlMap().selectOne("ExecutionPlan.selectJobOperationRecord", selectMap);
        if (Util.isNullOrEmpty(jobInfoMap)) {
            throw new CustomException("任务已更新，请刷新页面");
        }
        if (!Dict.JOB_STATE_WAITING.equals(jobInfoMap.get(Dict.JOBSTATE))) {
            throw new CustomException("立即执行失败");
        }
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String identificationId = null;
        MDC.put(Dict.MDCSCHEDULERSEQNO, String.valueOf(snowflakeIdWorker.nextId()));

        Map<String, Object> jobDetailMap = taskManagerService.selectDetail(requestMap);
        JobDataMap jobDataMap = (JobDataMap) jobDetailMap.get(Dict.JOBDATA);


        Map<String, Object> updateJobMap = new HashMap<>();
        updateJobMap.put(Dict.SCHEDULERSEQNO, MDC.get(Dict.MDCSCHEDULERSEQNO));
        updateJobMap.put(Dict.EXECUTORHANDLER, jobDataMap.getString(Dict.METHOD));
        updateJobMap.put(Dict.EXECUTORPARAM, jobDataMap.get(Dict.CUSTOMPARAM));
        updateJobMap.put(Dict.JOBID, jobId);

        try {
            identificationId = getFiredTriggerRecordId();
            JobKey jobKey = new JobKey(jobName, jobGroup);
            if (JobDynamicScheduler.checkExists(jobKey)) {
                //更新job状态为执行中
                Map<String, Object> updateMap = new HashMap<>();
                updateMap.put(Dict.JOBID, jobId);
                updateMap.put(Dict.JOBSTATE, Dict.JOB_STATE_EXECUTING);
                updateMap.put(Dict.ACQUIREDTIME, System.currentTimeMillis());
                updateMap.put(Dict.FIREDTIME, System.currentTimeMillis());
                updateMap.put(Dict.INSTANCENAME, JobDynamicScheduler.getSchedulerInstanceId());
                updateMap.put(Dict.IDENTIFICATIONID, identificationId);
                //立即执行标识
                updateMap.put(Dict.TRANSTYPE, 1);
                updateJobInfo(updateMap);

                jobTemplate.executorJob(jobId, jobDataMap);

                String executorIP = MDC.get("executorHost");
                updateJobMap.put(Dict.SCHEDULERRESULT, Dict.JOB_STATE_SUCCESS);
                updateJobMap.put(Dict.EXECUTORIP, executorIP);
                updateJobInfo(updateJobMap);
            }
        } catch (Exception e) {
            logger.error("trigger job error", e);
            if (jobId != null) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put(Dict.JOBID, jobId);
                errorMap.put(Dict.JOBSTATE, Dict.JOB_STATE_ERROR);
                errorMap.put(Dict.ACQUIREDTIME, System.currentTimeMillis());
                errorMap.put(Dict.FIREDTIME, System.currentTimeMillis());
                errorMap.put(Dict.IDENTIFICATIONID, identificationId);
                errorMap.put(Dict.ENDTIME, System.currentTimeMillis());
                errorMap.put(Dict.INSTANCENAME, JobDynamicScheduler.getSchedulerInstanceId());
                updateJobInfo(errorMap);
            }
            //记录执行日志
            updateJobMap.put(Dict.SCHEDULERRESULT, Dict.JOB_STATE_ERROR);
            updateJobInfo(updateJobMap);
            logger.error("ServiceName:{{}},JobName:{{}},JobTemplate is executing end.", jobDataMap.getString(Dict.SERVICEID), jobDataMap.getString(Dict.METHOD));
        }
    }

    private synchronized String getFiredTriggerRecordId() {
        long ftrCtr = System.currentTimeMillis();
        return JobDynamicScheduler.getSchedulerInstanceId() + ftrCtr++;
    }
}
