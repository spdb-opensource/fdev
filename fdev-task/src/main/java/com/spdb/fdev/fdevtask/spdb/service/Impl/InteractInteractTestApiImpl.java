package com.spdb.fdev.fdevtask.spdb.service.Impl;

import com.spdb.fdev.fdevtask.base.dict.Dict;
import com.spdb.fdev.fdevtask.spdb.service.InteractTestApi;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @program: fdev-process
 * @description:
 * @author: c-jiangl2
 * @create: 2021-03-25 17:15
 **/
@Service
@RefreshScope
public class InteractInteractTestApiImpl implements InteractTestApi {

    private Logger logger = LoggerFactory.getLogger(this.getClass());// 打印当前日志

    @Autowired
    private RestTransport restTransport;

    @Override
    public void newCreateTask(String taskId, String implUnitId, String taskName) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Dict.TASK_NO,taskId);
        params.put(Dict.UNIT_ID,implUnitId);
        params.put(Dict.TASKNAME,taskName);
        params.put(Dict.REST_CODE,"newCreateTask");
        try{
            restTransport.submit(params);
        }catch (Exception e){
            logger.info("研发单元工单未创建");
        }
    }

    @Override
    public void updateTaskUnitNo(String taskId, String taskName, String unitNo) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Dict.TASK_NO,taskId);
        params.put(Dict.UNIT_NO,unitNo);
        params.put(Dict.TASKNAME,taskName);
        params.put(Dict.REST_CODE,"updateTaskUnitNo");
        try{
            restTransport.submit(params);
        }catch (Exception e){
            logger.info("玉衡迁移工单失败");
        }
    }

    @Override
    public void deleteTask(String taskId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put(Dict.TASK_IDS,  Collections.singletonList(taskId));
        params.put(Dict.REST_CODE,"deleteTask");
        try{
            restTransport.submit(params);
        }catch (Exception e){
            logger.info("玉衡工单删除任务失败");
        }
    }
}
