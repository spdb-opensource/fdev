package com.spdb.quartz;

import com.spdb.common.config.database.MultiJdbcAccess;
import com.spdb.common.dict.Dict;
import com.spdb.common.globalseqno.SnowflakeIdWorker;
import com.spdb.common.schedule.JobDynamicScheduler;
import com.spdb.common.util.Util;
import com.spdb.job.service.IExecutionPlanService;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lizz
 */
@Configuration
public class JobTemplate extends QuartzJobBean {
    private static Logger logger = LoggerFactory.getLogger(JobTemplate.class);

    @Autowired
    SnowflakeIdWorker snowflakeIdWorker;
    @Autowired
    IExecutionPlanService executionPlanService;
    @Autowired
    private MultiJdbcAccess multiJdbcAccess;
    @Autowired
    RestTemplate restTemplate;



    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        Long jobId = null;
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        Trigger trigger = jobExecutionContext.getTrigger();
        MDC.put(Dict.MDCSCHEDULERSEQNO, String.valueOf(snowflakeIdWorker.nextId()));
        //查询待执行job
        Map<String, Object> resultMap = getExecutionJobInfo(trigger);
        try {
            //修改执行状态，获取IDateless
            jobId = getJobId(jobExecutionContext, jobId, trigger, resultMap);
            //执行JOB
            executorJob(jobId, jobDataMap);
            //记录执行信息
            updateSuccessExecutionPlanInfo(jobId, jobDataMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            updateErrorExecutionPlanInfo(jobId, jobDataMap);
        }
    }

    /**
     * 更新执行计划状态及信息-失败
     *
     * @param jobId
     * @param jobDataMap
     */
    private void updateErrorExecutionPlanInfo(Long jobId, JobDataMap jobDataMap) {
        if (jobId != null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put(Dict.JOBID, jobId);
            errorMap.put(Dict.JOBSTATE, Dict.JOB_STATE_ERROR);
            errorMap.put(Dict.ENDTIME, System.currentTimeMillis());
            executionPlanService.updateJobInfo(errorMap);
            logger.error("error update job info:{{}}", errorMap.toString());
        }

        //记录执行信息
        Map updateJobMap = new HashMap();
        updateJobMap.put(Dict.SCHEDULERSEQNO, MDC.get(Dict.MDCSCHEDULERSEQNO));
        updateJobMap.put(Dict.EXECUTORHANDLER, jobDataMap.getString(Dict.METHOD));
        updateJobMap.put(Dict.EXECUTORPARAM, jobDataMap.get(Dict.CUSTOMPARAM));
        updateJobMap.put(Dict.SCHEDULERRESULT, Dict.JOB_STATE_ERROR);
        updateJobMap.put(Dict.JOBID, jobId);
        executionPlanService.updateJobInfo(updateJobMap);
        logger.error("ServiceName:{{}},JobName:{{}},JobTemplate is executing end.", jobDataMap.getString(Dict.SERVICEID), jobDataMap.getString(Dict.METHOD));
    }

    /**
     * 组装uri
     *
     * @param jobDataMap
     * @return
     */
    public String createUri(JobDataMap jobDataMap) {
        String serviceId = jobDataMap.getString(Dict.SERVICEID);
        String contextPath = jobDataMap.getString(Dict.CONTEXTPATH);
        String method = jobDataMap.getString(Dict.METHOD);
        String url;
        if (null != contextPath && !"".equals(contextPath)) {
            url = "http://" + serviceId + "/" + contextPath + "/rest/" + method;
        } else {
            url = "http://" + serviceId + "/fdev-job-executor/rest/" + method;
        }
        return url;
    }

    /**
     * 更新执行计划状态及信息-成功
     *
     * @param jobId
     * @param jobDataMap
     */
    private void updateSuccessExecutionPlanInfo(Long jobId, JobDataMap jobDataMap) {
        String executorIP = MDC.get("executorHost");
        Map updateJobMap = new HashMap();
        updateJobMap.put(Dict.SCHEDULERSEQNO, MDC.get(Dict.MDCSCHEDULERSEQNO));
        updateJobMap.put(Dict.SCHEDULERRESULT, Dict.JOB_STATE_SUCCESS);
        updateJobMap.put(Dict.EXECUTORHANDLER, jobDataMap.getString(Dict.METHOD));
        updateJobMap.put(Dict.EXECUTORPARAM, jobDataMap.get(Dict.CUSTOMPARAM));
        updateJobMap.put(Dict.EXECUTORIP, executorIP);
        updateJobMap.put(Dict.JOBID, jobId);
        executionPlanService.updateJobInfo(updateJobMap);
    }


    /**
     * 修改执行状态，返回执行计划编号
     *
     * @param jobExecutionContext
     * @param jobId
     * @param trigger
     * @param resultMap
     * @return
     */
    private Long getJobId(JobExecutionContext jobExecutionContext, Long jobId, Trigger trigger, Map<String, Object> resultMap) {
        if (!Util.isNullOrEmpty(resultMap)) {
            //更新job状态为执行中
            Map<String, Object> updateMap = new HashMap<>();
            jobId = ((BigDecimal) resultMap.get(Dict.ID)).longValue();
            updateMap.put(Dict.JOBID, jobId);
            updateMap.put(Dict.JOBSTATE, Dict.JOB_STATE_EXECUTING);
            updateMap.put(Dict.FIREDTIME, System.currentTimeMillis());
            executionPlanService.updateJobInfo(updateMap);
        } else {
            JobKey jobKey = jobExecutionContext.getJobDetail().getKey();
            Map insertJobInfoMap = new HashMap<>();
            insertJobInfoMap.put(Dict.SCHEDTIME, trigger.getPreviousFireTime().getTime());
            insertJobInfoMap.put(Dict.TRIGGERNAME, trigger.getKey().getName());
            insertJobInfoMap.put(Dict.TRIGGERSTATE, Dict.JOB_STATE_EXECUTING);
            insertJobInfoMap.put(Dict.JOBNAME, jobKey.getName());
            insertJobInfoMap.put(Dict.JOBGROUP, jobKey.getGroup());
            insertJobInfoMap.put(Dict.INSTANCENAME, JobDynamicScheduler.getSchedulerInstanceId());
            insertJobInfoMap.put(Dict.IDENTIFICATIONID, jobExecutionContext.getFireInstanceId());
            insertJobInfoMap.put(Dict.ACQUIREDTIME, System.currentTimeMillis());
            insertJobInfoMap.put(Dict.FIREDTIME, System.currentTimeMillis());
            insertJobInfoMap.put(Dict.CREATEDATE, System.currentTimeMillis());
            executionPlanService.insertJobInfo(insertJobInfoMap);
            Map<String, Object> selectMap = new HashMap<>();
            selectMap.put(Dict.SCHEDTIME, trigger.getPreviousFireTime().getTime());
            selectMap.put(Dict.TRIGGERNAME, trigger.getKey().getName());
            selectMap.put(Dict.TRIGGERGROUP, trigger.getKey().getGroup());
            selectMap.put(Dict.JOBSTATE, Dict.JOB_STATE_EXECUTING);
            Map<String, Object> jobInfoOneMap = executionPlanService.selectJobInfoOne(selectMap);
            if (!Util.isNullOrEmpty(jobInfoOneMap)) {
                jobId = ((BigDecimal) jobInfoOneMap.get(Dict.ID)).longValue();
            }
        }
        return jobId;
    }

    /**
     * 获取执行job详情
     *
     * @param trigger
     * @return
     */
    public Map<String, Object> getExecutionJobInfo(Trigger trigger) {
        Map<String, Object> resultMap;
        if (trigger instanceof SimpleTrigger) {
            Map<String, Object> selectMap = new HashMap<>();
            selectMap.put(Dict.TRIGGERNAME, trigger.getKey().getName());
            selectMap.put(Dict.TRIGGERGROUP, trigger.getKey().getGroup());
            resultMap = executionPlanService.selectJobInfoOne(selectMap);
        } else {
            Map<String, Object> selectMap = new HashMap<>();
            selectMap.put(Dict.TRIGGERNAME, trigger.getKey().getName());
            selectMap.put(Dict.TRIGGERGROUP, trigger.getKey().getGroup());
            selectMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
            Map triggerMap = multiJdbcAccess.getSqlMap().selectOne("TaskManager.selectTrigger", selectMap);
            if (!Util.isNullOrEmpty(triggerMap.get(Dict.JOBID))) {
                selectMap.put(Dict.ID, triggerMap.get(Dict.JOBID));
            } else {
                selectMap.put(Dict.JOBSTATE, Dict.JOB_STATE_ACQUIRED);
                selectMap.put(Dict.SCHEDTIME, trigger.getPreviousFireTime().getTime());
            }
            triggerMap.put(Dict.JOBID, "");
            multiJdbcAccess.getSqlMap().update("ExecutionPlan.updateTrigger", triggerMap);
            resultMap = executionPlanService.selectJobInfoOne(selectMap);
        }
        return resultMap;
    }

    /**
     * 执行交易
     *
     * @param jobId
     * @param jobDataMap
     */
    public void executorJob(Object jobId, JobDataMap jobDataMap) {
        //开始调度任务
        logger.info("ServiceName:{{}},JobName:{{}},JobTemplate is executing begin.", jobDataMap.getString(Dict.SERVICEID), jobDataMap.getString(Dict.METHOD));
        //生成uri
        String url = createUri(jobDataMap);
        //http header配置
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(Dict.MDCSCHEDULERSEQNO, MDC.get(Dict.MDCSCHEDULERSEQNO));
        httpHeaders.add(Dict.CONTENTTYPE, "application/json");
        httpHeaders.add(Dict.JOBID, String.valueOf(jobId));
        logger.info("jobId = "+jobId);
        //http body配置
        Map<String, Object> multiValueMap = new HashMap<>();
        multiValueMap.put(Dict.CUSTOMPARAM, jobDataMap.get(Dict.CUSTOMPARAM));
        HttpEntity httpEntity = new HttpEntity(multiValueMap, httpHeaders);
        logger.info("url:"+url);
        logger.info("sendData:"+httpEntity.toString());
        restTemplate.postForEntity(url, httpEntity, String.class);
        logger.info("ServiceName:{{}},JobName:{{}},JobTemplate is executing end.", jobDataMap.getString(Dict.SERVICEID), jobDataMap.getString(Dict.METHOD));
    }

}
