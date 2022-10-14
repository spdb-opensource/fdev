package com.spdb.job.service.impl;

import com.spdb.common.dict.Constants;
import com.spdb.common.dict.Dict;
import com.spdb.common.exception.CustomException;
import com.spdb.common.schedule.JobDynamicScheduler;
import com.spdb.common.util.CronUtil;
import com.spdb.common.util.DateUtil;
import com.spdb.common.util.Util;
import com.spdb.common.config.database.MultiJdbcAccess;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.spdb.job.service.ITaskManagerService;
import org.quartz.*;
import org.quartz.impl.calendar.AnnualCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigDecimal;
import java.sql.Blob;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lizz
 */
@Service
public class TaskManagerServiceImpl implements ITaskManagerService {
    private static final Logger logger = LoggerFactory.getLogger(TaskManagerServiceImpl.class);

    @Autowired
    private MultiJdbcAccess multiJdbcAccess;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;

    @Override
    public void addJob(Map<String, Object> requestMap) {
        String schedulerType = (String) requestMap.get(Dict.SCHEDULERTYPE);
        JobDetail jobDetail = createJobDetail(requestMap);
        if (Dict.CRON.equals(schedulerType)) {
            Trigger trigger = createCronTrigger(requestMap);
            //注册调度任务
            JobDynamicScheduler.scheduleJob(jobDetail, trigger);
        } else if (Dict.SIMPLE.equals(schedulerType)) {
            fillTriggerJob(requestMap);
        } else {
            throw new CustomException(Constants.SCHEDULEREXCEPTION, new String[]{"注册"});
        }
        //启动任务
        JobDynamicScheduler.start();
    }

    @Override
    public Map<String, Object> selectTrigger(Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        Object description = requestMap.get(Dict.DESCRIPTION);
        String jobGroup = "common";
        String triggerGroup = (String) requestMap.get(Dict.TRIGGERGROUP);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Dict.JOBNAME, jobName);
        paramMap.put(Dict.JOBGROUP, jobGroup);
        paramMap.put(Dict.TRIGGERGROUP, triggerGroup);
        paramMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        paramMap.put(Dict.DESCRIPTION, description);

        int pageNumber = (int) requestMap.get(Dict.PAGENUMBER);
        int pageSize = (int) requestMap.get(Dict.PAGESIZE);
        PageHelper.startPage(pageNumber, pageSize);
        List<Map<String, Object>> rows = multiJdbcAccess.getSqlMap().selectList("TaskManager.selectTrigger", paramMap);

        Long startTime = (Long) requestMap.get(Dict.STARTTIME);
        Long endTime = (Long) requestMap.get(Dict.ENDTIME);
        if (startTime == null && endTime == null) {
            blobToObject(rows);
            resultMap.put("rows", rows);
            Page<Map<String, Object>> listPage = (Page<Map<String, Object>>) rows;
            resultMap.put(Dict.TOTAL, listPage.getTotal());
            resultMap.put(Dict.PAGENUMBER, listPage.getPageNum());
            resultMap.put(Dict.PAGESIZE, listPage.getPageSize());
            resultMap.put(Dict.PAGES, listPage.getPages());
            return resultMap;
        } else if (startTime == null && endTime != null) {
            startTime = DateUtil.getTodayStartTime().getTime();
            endTime = DateUtil.getDateEndTime(new Date(endTime)).getTime();
        } else if (startTime != null && endTime == null) {
            startTime = DateUtil.getDateStartTime(new Date(startTime)).getTime();
            endTime = DateUtil.getTodayEndTime().getTime();
        }

        Date start = DateUtil.getDateStartTime(new Date(startTime));
        Date end = DateUtil.getDateEndTime(new Date(endTime));
        Iterator<Map<String, Object>> row = rows.iterator();
        while (row.hasNext()){
            Map map = row.next();
            if (Dict.JOB_TRIGGERTYPE_CRON.equals(map.get(Dict.TRIGGERTYPE))) {
                List<Long> cronList = CronUtil.getNextExecTimeBetween((String) map.get(Dict.CRONEXPRESSION), null, start, end);
                if (Util.isNullOrEmpty(cronList)) {
                   row.remove();
                }
            } else {
                if (!DateUtil.isEffectiveDate(((BigDecimal) map.get(Dict.NEXTFIRETIME)).longValue(), startTime, DateUtil.getDateEndTime(new Date(endTime)).getTime())) {
                    row.remove();
                }
            }
        }
        
        blobToObject(rows);
        resultMap.put("rows", rows);
        Page<Map<String, Object>> listPage = (Page<Map<String, Object>>) rows;
        resultMap.put(Dict.TOTAL, listPage.getTotal());
        resultMap.put(Dict.PAGENUMBER, listPage.getPageNum());
        resultMap.put(Dict.PAGESIZE, listPage.getPageSize());
        resultMap.put(Dict.PAGES, listPage.getPages());
        return resultMap;
    }

    @Override
    public void deleteJob(Map<String, Object> requestMap) {
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        String triggerGroup = (String) requestMap.get(Dict.TRIGGERGROUP);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Dict.JOBNAME, jobName);
        paramMap.put(Dict.JOBGROUP, jobGroup);
        paramMap.put(Dict.TRIGGERNAME, triggerName);
        paramMap.put(Dict.TRIGGERGROUP, triggerGroup);
        paramMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        Map<String, Object> map = multiJdbcAccess.getSqlMap().selectOne("TaskManager.selectTrigger", paramMap);
        long nextFireTime = ((BigDecimal) map.get(Dict.NEXTFIRETIME)).longValue();
        if (nextFireTime - System.currentTimeMillis() < 5 * 60 * 1000) {
            throw new CustomException("五分钟之内将要执行的任务不能删除");
        }

        //TriggerKey定义了trigger的名称和组别
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroup);

        //暂停触发器
        JobDynamicScheduler.resumeTrigger(triggerKey);

        //移除触发器
        JobDynamicScheduler.unscheduleJob(triggerKey);
        Map<String, Object> selectMap = new HashMap<>();
        selectMap.put(Dict.JOBNAME, jobName);
        selectMap.put(Dict.JOBGROUP, jobGroup);
        selectMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        List<Map> row = multiJdbcAccess.getSqlMap().selectList("TaskManager.selectTrigger", selectMap);
        if (Util.isNullOrEmpty(row)) {
            //移除任务
            JobDynamicScheduler.deleteJob(JobKey.jobKey(jobName, jobGroup));
        }

        Map<String, Object> deleteJobInfoMap = new HashMap<>();
        deleteJobInfoMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        deleteJobInfoMap.put(Dict.JOBSTATE, Dict.JOB_STATE_WAITING);
        deleteJobInfoMap.put(Dict.JOBNAME, jobName);
        deleteJobInfoMap.put(Dict.JOBGROUP, jobGroup);
        deleteJobInfoMap.put(Dict.TRIGGERNAME, triggerName);
        deleteJobInfoMap.put(Dict.TRIGGERGROUP, triggerGroup);
        multiJdbcAccess.getSqlMap().delete("ExecutionPlan.deleteJobOperationRecord", deleteJobInfoMap);
        multiJdbcAccess.getSqlMap().delete("PausedStrategyManager.deletePausedJob", deleteJobInfoMap);
    }

    @Override
    public void updateJob(Map<String, Object> requestMap) {
        String triggerType = (String) requestMap.get(Dict.TRIGGERTYPE);
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put(Dict.JOBNAME, jobName);
        paramMap.put(Dict.JOBGROUP, jobGroup);
        paramMap.put(Dict.TRIGGERNAME, triggerName);
        paramMap.put(Dict.TRIGGERGROUP, jobGroup);
        paramMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        Map<String, Object> map = multiJdbcAccess.getSqlMap().selectOne("TaskManager.selectTrigger", paramMap);
//        long nextFireTime = ((BigDecimal) map.get(Dict.NEXTFIRETIME)).longValue();
//        if ((nextFireTime - System.currentTimeMillis()) < 5 * 60 * 1000) {
//            throw new CustomException("五分钟之内将要执行的任务不能修改");
//        }

        Object cronExpression = requestMap.get(Dict.CRONEXPRESSION);
        //构建旧的TriggerKey
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, jobGroup);
        HashSet<Trigger> triggerSet = new HashSet<>();
        if (Dict.JOB_TRIGGERTYPE_CRON.equals(triggerType)) {
            //通过cron表达式构建CronScheduleBuilder
            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule((String) cronExpression);
            //从调度容器中获取旧的CronTrigger
            CronTrigger trigger = (CronTrigger) JobDynamicScheduler.getTrigger(triggerKey);
            Integer misfireInstr = (Integer) requestMap.get(Dict.MISFIREINSTR);
            CronScheduleBuilder cronScheduleBuilder;
            if (-1 == misfireInstr) {
                cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
            } else if (1 == misfireInstr) {
                cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
            } else if (2 == misfireInstr) {
                cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionDoNothing();
            } else {
                cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
            }

            /*
             * 更新CronTrigger
             * triggerKey 工作项1：job trigger名以及所属组生成的triggerKey
             * scheduleBuilder 工作项2：指定调度参数
             */
            trigger = trigger.getTriggerBuilder()
                    .withIdentity(triggerKey).withSchedule(scheduleBuilder)
                    .withSchedule(cronScheduleBuilder)
                    .build();
            //更新调度任务
            JobDynamicScheduler.rescheduleJob(triggerKey, trigger);
            triggerSet.add(trigger);
        } else if (Dict.JOB_TRIGGERTYPE_SIMPLE.equals(triggerType)) {
            long schedTime = (long) cronExpression;
            SimpleTrigger trigger = (SimpleTrigger) JobDynamicScheduler.getTrigger(triggerKey);
            trigger = trigger.getTriggerBuilder()
                    .withIdentity(triggerKey)
                    .startAt(new Date(schedTime))
                    .build();
            JobDynamicScheduler.rescheduleJob(triggerKey, trigger);
            triggerSet.add(trigger);
        } else {
            throw new CustomException("TriggerType 类型错误");
        }

        JobKey jobKey = new JobKey(jobName, jobGroup);
        JobDetail jobDetail = JobDynamicScheduler.getJobDetail(jobKey);
        String description = (String) requestMap.get(Dict.DESCRIPTION);
        jobDetail = jobDetail.getJobBuilder().withDescription(description).build();
        JobDynamicScheduler.scheduleJob(jobDetail, triggerSet, true);
        //启动任务
        JobDynamicScheduler.start();
    }

    @Override
    public Map<String, Object> selectJobDetail(Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        Map<String, Object> jobDetailMap = new HashMap<>();
        jobDetailMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobDetailMap.put(Dict.JOBNAME, jobName);
        jobDetailMap.put(Dict.JOBGROUP, jobGroup);
        String triggerType = (String) requestMap.get(Dict.TRIGGERTYPE);
        //List<Map<String, Object>> rows = multiJdbcAccess.getSqlMap().selectList("TaskManager.selectJobDetail", jobDetailMap);
        //详情修改后sql
        Map<String, Object> row = multiJdbcAccess.getSqlMap().selectOne("TaskManager.selectJobDetail", jobDetailMap);
        blobToObject(row);
        List<Map<String, Object>> jobList = multiJdbcAccess.getSqlMap().selectList("TaskManager.selectpausedTriggerDateJobList", jobDetailMap);
        if(!Util.isNullOrEmpty(row)){
            row.put(Dict.JOBLIST, jobList);
        }
        return row;
    }

    @Override
    public Map<String, Object> selectDetail(Map<String, Object> requestMap) {
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = (String) requestMap.get(Dict.JOBGROUP);
        Map<String, Object> jobDetailMap = new HashMap<>();
        jobDetailMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        jobDetailMap.put(Dict.JOBNAME, jobName);
        jobDetailMap.put(Dict.JOBGROUP, jobGroup);
        Map<String, Object> row = multiJdbcAccess.getSqlMap().selectOne("TaskManager.selectDetail", jobDetailMap);
        blobToObject(row);
        return row;
    }

    @Override
    public void pauseJob(Map<String, Object> requestMap) {
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, jobGroup);
        JobDynamicScheduler.pauseTrigger(triggerKey);

        Map<String, Object> pausedMap = new HashMap<>();
        pausedMap.put(Dict.JOBNAME, jobName);
        pausedMap.put(Dict.JOBGROUP, jobGroup);
        pausedMap.put(Dict.TRIGGERNAME, triggerName);
        pausedMap.put(Dict.JOBSTATE, Dict.JOB_STATE_PAUSED);
        pausedMap.put(Dict.JOBSTATEOLD, Dict.JOB_STATE_WAITING);
        pauseAndResumeHelp(pausedMap);
    }

    @Override
    public void resumeJob(Map<String, Object> requestMap) {
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, jobGroup);
        JobDynamicScheduler.resumeTrigger(triggerKey);

        Map<String, Object> resumeMap = new HashMap<>();
        resumeMap.put(Dict.JOBNAME, jobName);
        resumeMap.put(Dict.JOBGROUP, jobGroup);
        resumeMap.put(Dict.TRIGGERNAME, triggerName);
        resumeMap.put(Dict.JOBSTATE, Dict.JOB_STATE_WAITING);
        resumeMap.put(Dict.JOBSTATEOLD, Dict.JOB_STATE_PAUSED);
        //任务类型（0正常，1、立即执行，2取消的任务,3、延期任务，4、一次性任务）
        resumeMap.put(Dict.TRANSTYPEOLD, 2);
        pauseAndResumeHelp(resumeMap);
    }

    @Override
    public void pauseAndResumeHelp(Map<String, Object> requestMap) {
        String jobState = (String) requestMap.get(Dict.JOBSTATE);
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        String jobStateOld = (String) requestMap.get(Dict.JOBSTATEOLD);
        Object transTypeOld = requestMap.get(Dict.TRANSTYPEOLD);
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        updateMap.put(Dict.JOBSTATE, jobState);
        //updateMap.put("nowTime", System.currentTimeMillis());
        updateMap.put(Dict.JOBNAME, jobName);
        updateMap.put(Dict.JOBGROUP, jobGroup);
        updateMap.put(Dict.TRIGGERNAME, triggerName);
        updateMap.put(Dict.TRIGGERGROUP, jobGroup);
        updateMap.put(Dict.JOBSTATEOLD, jobStateOld);
        updateMap.put(Dict.TRANSTYPEOLD, transTypeOld);
        multiJdbcAccess.getSqlMap().update("ExecutionPlan.updateJobOperationRecord", updateMap);
    }

    @Override
    public void fillTriggerJob(Map<String, Object> requestMap) {
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        JobKey jobKey = new JobKey(jobName, jobGroup);
        if (!JobDynamicScheduler.checkExists(jobKey)) {
            throw new CustomException(Constants.IS_NOT_EXIST, new String[]{"Job"});
        }

        String triggerName = Dict.SIMPLE + jobName + System.currentTimeMillis();
        String triggerGroup = (String) requestMap.get(Dict.JOBGROUP);
        TriggerKey triggerKey = new TriggerKey(triggerName, triggerGroup);
        if (JobDynamicScheduler.checkExists(triggerKey)) {
            throw new CustomException(Constants.IS_EXIST, new String[]{"Trigger"});
        }
        long nextFireTime = (long) requestMap.get(Dict.NEXTFIRETIME);
        long prevFireTime = -1;
        int priority = 5;
        String triggerState = Dict.JOB_STATE_WAITING;
        String triggerType = Dict.JOB_TRIGGERTYPE_SIMPLE;
        long endTime = 0;
        int misfireInstr = 2;
        int repeatCount = 0;
        int repeatInterval = 0;
        int timesTriggered = 0;

        Map<String, Object> triggerMap = new HashMap<>();
        triggerMap.put(Dict.JOBNAME, jobName);
        triggerMap.put(Dict.JOBGROUP, jobGroup);
        triggerMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        triggerMap.put(Dict.TRIGGERNAME, triggerName);
        triggerMap.put(Dict.TRIGGERGROUP, triggerGroup);
        triggerMap.put(Dict.NEXTFIRETIME, nextFireTime);
        triggerMap.put(Dict.PREVFIRETIME, prevFireTime);
        triggerMap.put(Dict.PRIORITY, priority);
        triggerMap.put(Dict.TRIGGERSTATE, triggerState);
        triggerMap.put(Dict.TRIGGERTYPE, triggerType);
        triggerMap.put(Dict.STARTTIME, nextFireTime);
        triggerMap.put(Dict.ENDTIME, endTime);
        triggerMap.put(Dict.MISFIREINSTR, misfireInstr);

        int i = multiJdbcAccess.getSqlMap().insert("TaskManager.insertTrigger", triggerMap);
        if (i > 0) {
            Map<String, Object> simpleTriggerMap = new HashMap<>();
            simpleTriggerMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
            simpleTriggerMap.put(Dict.TRIGGERNAME, triggerName);
            simpleTriggerMap.put(Dict.TRIGGERGROUP, triggerGroup);
            simpleTriggerMap.put(Dict.REPEATCOUNT, repeatCount);
            simpleTriggerMap.put(Dict.REPEATINTERVAL, repeatInterval);
            simpleTriggerMap.put(Dict.TIMESTRIGGERED, timesTriggered);
            int j = multiJdbcAccess.getSqlMap().insert("TaskManager.insertSimpleTrigger", simpleTriggerMap);
            if (j > 0) {
                Map<String, Object> recordMap = new HashMap<>();
                recordMap.put(Dict.JOBNAME, jobName);
                recordMap.put(Dict.JOBGROUP, jobGroup);
                recordMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
                recordMap.put(Dict.TRIGGERNAME, triggerName);
                recordMap.put(Dict.TRIGGERGROUP, triggerGroup);
                recordMap.put(Dict.SCHEDTIME, nextFireTime);
                recordMap.put(Dict.JOBSTATE, triggerState);
                //任务类型（0正常，1、立即执行，2取消的任务,3、延期任务，4、一次性任务）
                recordMap.put(Dict.TRANSTYPE, 4);
                recordMap.put(Dict.CREATEDATE, System.currentTimeMillis());
                multiJdbcAccess.getSqlMap().insert("ExecutionPlan.insertJobOperationRecord", recordMap);
            }
        }
    }

    /**
     * 构建Job元数据
     *
     * @param requestMap 请求参数
     * @return 返回
     */
    private JobDetail createJobDetail(Map<String, Object> requestMap) {
        /*
         * 构建JobDetail(表示一个具体的可执行的调度程序,Job是这个可执行程调度程序所要执行的内容)
         * jobClass 工作项1：Job类
         * jobName, jobGroupName 工作项2：job名以及所属组
         */
        Class<? extends Job> jobClass = getJobClass();
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String description = (String) requestMap.get(Dict.DESCRIPTION);
        boolean replace = true;

        JobKey jobKey = new JobKey(jobName, jobGroup);
        if (JobDynamicScheduler.checkExists(jobKey)) {
            return JobDynamicScheduler.getJobDetail(jobKey);
        }

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(Dict.SERVICEID, requestMap.get(Dict.SERVICEID));
        jobDataMap.put(Dict.CONTEXTPATH, requestMap.get(Dict.CONTEXTPATH));
        jobDataMap.put(Dict.METHOD, requestMap.get(Dict.METHOD));
        jobDataMap.put(Dict.CUSTOMPARAM, requestMap.get(Dict.CUSTOMPARAM));

        /*
         * 构建JobDetail(表示一个具体的可执行的调度程序,Job是这个可执行程调度程序所要执行的内容)
         * jobClass 工作项1：Job类
         * jobName, jobGroupName 工作项2：job名以及所属组
         */
        return JobBuilder.newJob(jobClass)
                .withIdentity(jobName, jobGroup)
                .withDescription(description)
                .usingJobData(jobDataMap)
                .storeDurably(replace)
                .build();
    }

    /**
     * 构建Cron类型的触发器
     * 构建触发器Trigger(调度参数的配置，代表何时触发该任务)
     *
     * @param requestMap 请求参数
     * @return 返回
     */
    private Trigger createCronTrigger(Map<String, Object> requestMap) {
        /*
         * 构建CronTrigger触发器
         * triggerName, jobGroupName 工作项1：job trigger名以及所属组
         * scheduleBuilder 工作项2：指定调度参数,通过cron表达式构建CronScheduleBuilder
         */
        String triggerName = (String) requestMap.get(Dict.SCHEDULERTYPE) + requestMap.get(Dict.JOBNAME);
        String triggerGroup = "common";
        String cronExpression = (String) requestMap.get(Dict.CRONEXPRESSION);
        List<Long> cronList = CronUtil.getNextExecTime(cronExpression, 2);
//        if (cronList.size() >= 2 && (cronList.get(1) - cronList.get(0)) < 5 * 60 * 1000) {
//            throw new CustomException("任务触发间隔不能小于五分钟");
//        }
        if (Util.isNullOrEmpty(cronList)) {
            throw new CustomException(Constants.PARAM_ERROR,"请求参数"+cronExpression+"异常!");
        }
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression);
        String calendarName = (String) requestMap.get(Dict.CALENDARNAME);
        CronScheduleBuilder cronScheduleBuilder;
        Integer misfireInstr = (Integer) requestMap.get(Dict.MISFIREINSTR);
        if (-1 == misfireInstr) {
            cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionIgnoreMisfires();
        } else if (1 == misfireInstr) {
            cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        } else if (2 == misfireInstr) {
            cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionDoNothing();
        } else {
            cronScheduleBuilder = scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        }

        return TriggerBuilder.newTrigger()
                .withIdentity(triggerName, triggerGroup)
                .withSchedule(cronScheduleBuilder)
                .modifiedByCalendar(calendarName)
                .build();
    }

    @Override
    public Map<String, Object> getServiceList() {
        Map<String, Object> resultMap = new HashMap<>();
        String url = null;
        try {
            List<String> serviceList = discoveryClient.getServices();
            String name = "job-executor";
            Pattern pattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
            for (String str : serviceList) {
                Matcher matcher = pattern.matcher(str);
                if (matcher.find()) {
                    url = "http://" + str + "/fdev-job-executor/GetTransList";
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.add(Dict.CONTENTTYPE, "application/json");
                    httpHeaders.set("source", "back");
                    MultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
                    HttpEntity httpEntity = new HttpEntity(multiValueMap, httpHeaders);
                    ResponseEntity<String> result = restTemplate.postForEntity(url, httpEntity, String.class);
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map resultBody = objectMapper.readValue(result.getBody(), Map.class);
                    resultMap.put(str, resultBody.get("TransList"));
                }
            }
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException){
                if (((HttpClientErrorException)e).getStatusCode().value() >= HttpStatus.BAD_REQUEST.value()) {
                    logger.warn("service url:{{}} ,code:{{}}", url, ((HttpClientErrorException)e).getRawStatusCode());
                }
            }else{
                logger.error(e.getMessage(), e);
            }
        }

        return resultMap;
    }


    private Class<? extends Job> getJobClass() {
        try {
            return (Class<? extends Job>) Class.forName(Constants.JOB_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            logger.error("Load JobTemplate Exception", e);
            throw new CustomException(Constants.CLASSNOTFOUNDEXCEPTION, new String[]{"template"});
        }
    }

    private void blobToObject(Object object) {
        if (object instanceof List) {
            for (Map<String, Object> map : (List<Map<String, Object>>) object) {

                try (InputStream input = ((Blob) map.get(Dict.JOBDATA)).getBinaryStream();
                     ObjectInputStream in = new ObjectInputStream(input);) {
                    Object obj = in.readObject();
                    if (obj instanceof AnnualCalendar) {
                        map.put("calendar", ((AnnualCalendar) obj).getDaysExcluded());
                    } else {
                        map.put(Dict.JOBDATA, new JobDataMap((Map<?, ?>) obj));
                    }
                } catch (Exception e) {
                    throw new CustomException(e.getMessage());
                }
            }
        }else if (object instanceof Map) {
            try (InputStream input = ((Blob) ((Map) object).get(Dict.JOBDATA)).getBinaryStream();
                 ObjectInputStream in = new ObjectInputStream(input);) {
                Object obj = in.readObject();
                ((Map) object).put(Dict.JOBDATA, new JobDataMap((Map<?, ?>) obj));
            } catch (Exception e) {
                throw new CustomException(e.getMessage());
            }
        }

    }

}