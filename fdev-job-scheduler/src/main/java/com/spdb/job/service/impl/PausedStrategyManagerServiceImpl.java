package com.spdb.job.service.impl;

import com.spdb.common.dict.Constants;
import com.spdb.common.dict.Dict;
import com.spdb.common.exception.CustomException;
import com.spdb.common.schedule.JobDynamicScheduler;
import com.spdb.common.util.CronUtil;
import com.spdb.common.util.DateUtil;
import com.spdb.common.util.Util;
import com.spdb.common.config.database.MultiJdbcAccess;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.spdb.job.service.IPausedStrategyManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author lizz
 */
@Service
public class PausedStrategyManagerServiceImpl implements IPausedStrategyManagerService {
    private static final Logger logger = LoggerFactory.getLogger(PausedStrategyManagerServiceImpl.class);

    @Autowired
    private MultiJdbcAccess multiJdbcAccess;

    @Override
    public void insertPauseDateJob(Map<String, Object> requestMap) {
        String strategyName = (String) requestMap.get(Dict.STRATEGYNAME);
        Long startPausedDate = (Long) requestMap.get(Dict.STARTTIME);
        Long endPausedDate = (Long) requestMap.get(Dict.ENDTIME);
        String jobGroup = "common";
        if (startPausedDate > endPausedDate) {
            throw new CustomException("开始时间不能大于结束时间");
        }
        Map<String, Object> pauseDateJobMap = new HashMap<>();
        pauseDateJobMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        pauseDateJobMap.put(Dict.STARTPAUSEDDATE, startPausedDate);
        pauseDateJobMap.put(Dict.ENDPAUSEDDATE, endPausedDate);
        pauseDateJobMap.put(Dict.JOBGROUP, jobGroup);
        List<Map<String, Object>> rowMapList = multiJdbcAccess.getSqlMap().selectList("PausedStrategyManager.selectpausedTriggerDate", pauseDateJobMap);
        if (rowMapList.size() > 0) {
            throw new CustomException("暂停策略添加重复");
        }
        //增加暂停策略唯一性
        List<Map<String, Object>> uniqueList = multiJdbcAccess.getSqlMap().selectList("PausedStrategyManager.selectpausedTrigger", pauseDateJobMap);
        if(uniqueList.size() > 0 && !Util.isNullOrEmpty(uniqueList)){
            for(int i=0 ; i<uniqueList.size() ; i++){
                Long startPausedDateDB = ((BigDecimal) uniqueList.get(i).get(Dict.STARTPAUSEDDATE)).longValue();
                Long endPausedDateDB = ((BigDecimal) uniqueList.get(i).get(Dict.ENDPAUSEDDATE)).longValue();
                if((DateUtil.isEffectiveDate(startPausedDate, startPausedDateDB, endPausedDateDB)) ||
                        DateUtil.isEffectiveDate(endPausedDate, startPausedDateDB, endPausedDateDB)){
                    throw new CustomException(Constants.PAUSED_DATE_ERROR,"暂停日期需保持唯一性");
                }
            }
        }

        pauseDateJobMap.put(Dict.STRATEGYNAME, strategyName);
        multiJdbcAccess.getSqlMap().insert("PausedStrategyManager.insertpausedTriggerDate", pauseDateJobMap);
        Map<String, Object> rowMap = multiJdbcAccess.getSqlMap().selectOne("PausedStrategyManager.selectpausedTriggerDate", pauseDateJobMap);
        Long pausedDateId = ((BigDecimal) rowMap.get(Dict.ID)).longValue();
        List<Map<String, Object>> rows = multiJdbcAccess.getSqlMap().selectList("TaskManager.selectTrigger", pauseDateJobMap);
        for (Map map : rows) {
            boolean flag = true;
            String cronExpression = (String) map.get(Dict.CRONEXPRESSION);
            if(Util.isNullOrEmpty(cronExpression)){
                continue;
            }
            List<Long> cronList = CronUtil.getNextExecTimeBetween(cronExpression, null, new Date(startPausedDate), new Date(endPausedDate));
            for (long date : cronList) {
                if (flag && date >= startPausedDate && date <= endPausedDate) {
                    Map<String, Object> paramMap = new HashMap<>();
                    paramMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
                    paramMap.put(Dict.PAUSEDDATEID, pausedDateId);
                    paramMap.put(Dict.TRIGGERGROUP, map.get(Dict.TRIGGERGROUP));
                    paramMap.put(Dict.TRIGGERNAME, map.get(Dict.TRIGGERNAME));
                    paramMap.put(Dict.JOBGROUP, map.get(Dict.JOBGROUP));
                    paramMap.put(Dict.JOBNAME, map.get(Dict.JOBNAME));
                    multiJdbcAccess.getSqlMap().insert("PausedStrategyManager.insertpausedTriggerDateJob", paramMap);
                    flag = false;
                }
            }
        }
    }

    @Override
    public Map<String, Object> selectPausedTriggerBetweenDate(Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        Object schedTime = requestMap.get(Dict.SCHEDTIME);
        Long startTime = null;
        if(!Util.isNullOrEmpty(requestMap.get(Dict.STARTTIME))){
            startTime = (Long) requestMap.get(Dict.STARTTIME);
        }
        Long endTime = null;;
        if(!Util.isNullOrEmpty(requestMap.get(Dict.ENDTIME))){
            endTime = (Long) requestMap.get(Dict.ENDTIME);
        }
        String jobGroup = "common";
        String strategyName = (String) requestMap.get(Dict.STRATEGYNAME);
        String scope = (String) requestMap.get("scope");
        Map<String, Object> pauseDateJobMap = new HashMap<>();
        pauseDateJobMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        pauseDateJobMap.put(Dict.STARTPAUSEDDATE, startTime);
        pauseDateJobMap.put(Dict.ENDPAUSEDDATE, endTime == null ? null : DateUtil.getDateEndTime(new Date(endTime)).getTime());
        pauseDateJobMap.put(Dict.SCHEDTIME, schedTime);
        pauseDateJobMap.put(Dict.JOBGROUP, jobGroup);
        pauseDateJobMap.put(Dict.STRATEGYNAME, strategyName);
        //有效策略
        if ("1".equals(scope)){
            Long validTime =  System.currentTimeMillis();
            pauseDateJobMap.put("validTime",validTime);
        }
        //失效策略
        if ("0".equals(scope)){
            Long failureTime =  System.currentTimeMillis();
            pauseDateJobMap.put("failureTime",failureTime);
        }
        Object pageNumber = requestMap.get(Dict.PAGENUMBER);
        Object pageSize = requestMap.get(Dict.PAGESIZE);
        if (pageNumber != null && pageSize != null) {
            PageHelper.startPage((int) pageNumber, (int) pageSize);
        }

        List<Map<String, Object>> rows = multiJdbcAccess.getSqlMap().selectList("PausedStrategyManager.selectpausedTriggerBetweenDate", pauseDateJobMap);
        resultMap.put("rows", rows);
        if (pageNumber != null && pageSize != null) {
            Page<Map<String, Object>> listPage = (Page<Map<String, Object>>) rows;
            resultMap.put(Dict.TOTAL, listPage.getTotal());
            resultMap.put(Dict.PAGENUMBER, listPage.getPageNum());
            resultMap.put(Dict.PAGESIZE, listPage.getPageSize());
            resultMap.put(Dict.PAGES, listPage.getPages());
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> selectPausedTriggerDateAndJob(Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> pausedStrategyMap = selectPausedTriggerBetweenDate(requestMap);
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        List<Map> mapList = (List<Map>) pausedStrategyMap.get("rows");
        Iterator<Map> iterator = mapList.iterator();
        while (iterator.hasNext()) {
            Map<String, Object> map = iterator.next();
            Map<String, Object> pausedDateJobMap = new HashMap<>();
            long id = ((BigDecimal) map.get(Dict.ID)).longValue();
            //0有效1失效
            Integer stateType = 0;
            pausedDateJobMap.put(Dict.PAUSEDDATEID, id);
            pausedDateJobMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
            pausedDateJobMap.put(Dict.JOBNAME, jobName);
            pausedDateJobMap.put(Dict.JOBGROUP, jobGroup);
            pausedDateJobMap.put(Dict.STATETYPE, stateType);
            int pageNumber = (int) requestMap.get(Dict.PAGENUMBER);
            int pageSize = (int) requestMap.get(Dict.PAGESIZE);
            PageHelper.startPage(pageNumber, pageSize);
            List<Map> pausedJobList = multiJdbcAccess.getSqlMap().selectList("PausedStrategyManager.selectPausedTriggerDateJob", pausedDateJobMap);
            if (!Util.isNullOrEmpty(jobName)) {
                if (Util.isNullOrEmpty(pausedJobList)) {
                    iterator.remove();
                } else {
                    map.put(Dict.JOBLIST, pausedJobList);
                }
            } else {
                map.put(Dict.JOBLIST, pausedJobList);
            }
        }
        resultMap.put(Dict.TOTAL, pausedStrategyMap.get(Dict.TOTAL));
        resultMap.put(Dict.PAGENUMBER, pausedStrategyMap.get(Dict.PAGENUMBER));
        resultMap.put(Dict.PAGESIZE, pausedStrategyMap.get(Dict.PAGESIZE));
        resultMap.put(Dict.PAGES, pausedStrategyMap.get(Dict.PAGES));
        resultMap.put("rows", mapList);
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> selectPausedStrategy(Map<String, Object> requestMap) {
        requestMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        return multiJdbcAccess.getSqlMap().selectList("PausedStrategyManager.selectPausedTriggerDateJob", requestMap);
    }

    @Override
    public Map<String, Object> selectJobAndpausedDate(Map<String, Object> requestMap) {
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.STARTPAUSEDDATE, requestMap.get(Dict.STARTTIME));
        map.put(Dict.ENDPAUSEDDATE, requestMap.get(Dict.ENDTIME));
        map.put(Dict.JOBNAME, requestMap.get(Dict.JOBNAME));
        map.put(Dict.JOBGROUP, requestMap.get(Dict.JOBGROUP));
        map.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        int pageNumber = (int) requestMap.get(Dict.PAGENUMBER);
        int pageSize = (int) requestMap.get(Dict.PAGESIZE);
        String scope = (String) requestMap.get("scope");
        //失效策略
        if ("0".equals(scope)) {
            Long failureTime = System.currentTimeMillis();
            map.put("failureTime", failureTime);
        }
        //有效策略
        if ("1".equals(scope)) {
            Long validTime = System.currentTimeMillis();
            map.put("validTime", validTime);
        }
        PageHelper.startPage(pageNumber, pageSize);
        List<Map<String, Object>> rows = multiJdbcAccess.getSqlMap().selectList("PausedStrategyManager.selectJobAndpausedDate", map);
        resultMap.put("rows", rows);
        Page<Map<String, Object>> listPage = (Page<Map<String, Object>>) rows;
        resultMap.put(Dict.TOTAL, listPage.getTotal());
        resultMap.put(Dict.PAGENUMBER, listPage.getPageNum());
        resultMap.put(Dict.PAGESIZE, listPage.getPageSize());
        resultMap.put(Dict.PAGES, listPage.getPages());
        return resultMap;
    }

    @Override
    public void deletePausedStrategy(Map<String, Object> requestMap) {
        Integer id = (Integer) requestMap.get(Dict.ID);
        Map<String, Object> deleteMap = new HashMap<>();
        deleteMap.put(Dict.ID, id);
        deleteMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        multiJdbcAccess.getSqlMap().delete("PausedStrategyManager.deletePausedJob", deleteMap);
        multiJdbcAccess.getSqlMap().delete("PausedStrategyManager.deletePausedStrategy", deleteMap);
    }

    @Override
    public int deletePausedStrategyJob(Map<String, Object> requestMap) {
        Object pausedDateId = requestMap.get(Dict.PAUSEDDATEID);
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String jobGroup = "common";
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        String triggerGroup = (String) requestMap.get(Dict.TRIGGERGROUP);
        Map<String, Object> deleteMap = new HashMap<>();
        deleteMap.put(Dict.PAUSEDDATEID, pausedDateId);
        //0有效1失效
        deleteMap.put(Dict.STATETYPE, 1);
        deleteMap.put(Dict.JOBNAME, jobName);
        deleteMap.put(Dict.JOBGROUP, jobGroup);
        deleteMap.put(Dict.TRIGGERNAME, triggerName);
        deleteMap.put(Dict.TRIGGERGROUP, triggerGroup);
        deleteMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        return multiJdbcAccess.getSqlMap().delete("PausedStrategyManager.updatePausedStrategyJob", deleteMap);
    }

    @Override
    public int recoverPausedStrategyJob(Map<String, Object> requestMap) {
        Object pausedDateId = requestMap.get(Dict.PAUSEDDATEID);
        String jobGroup = "common";
        String jobName = (String) requestMap.get(Dict.JOBNAME);
        String triggerGroup = (String) requestMap.get(Dict.TRIGGERGROUP);
        String triggerName = (String) requestMap.get(Dict.TRIGGERNAME);
        Map<String, Object> deleteMap = new HashMap<>();
        deleteMap.put(Dict.PAUSEDDATEID, pausedDateId);
        //0有效1失效
        deleteMap.put(Dict.STATETYPE, 0);
        deleteMap.put(Dict.JOBNAME, jobName);
        deleteMap.put(Dict.JOBGROUP, jobGroup);
        deleteMap.put(Dict.TRIGGERNAME, triggerName);
        deleteMap.put(Dict.TRIGGERGROUP, triggerGroup);
        deleteMap.put(Dict.SCHEDNAME, JobDynamicScheduler.getSchedulerName());
        return multiJdbcAccess.getSqlMap().delete("PausedStrategyManager.updatePausedStrategyJob", deleteMap);
    }
}
