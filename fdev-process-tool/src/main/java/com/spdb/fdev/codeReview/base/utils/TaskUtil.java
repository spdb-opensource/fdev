package com.spdb.fdev.codeReview.base.utils;

import com.spdb.fdev.codeReview.base.dict.Dict;
import com.spdb.fdev.codeReview.spdb.dto.TaskEntity;
import com.spdb.fdev.common.exception.FdevException;
import com.spdb.fdev.transport.RestTransport;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.logging.Logger;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
public class TaskUtil {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TaskUtil.class);
    @Autowired
    private RestTransport restTransport;

    public List<Map> queryTasksSimpleByIds(Set<String> taskIds){
        Map<String, Object> map=new HashMap();
        map.put(Dict.IDS,taskIds);
        map.put(Dict.REST_CODE, Dict.QUERYTASKSSIMPLEBYIDS);
        try {
            return (List<Map>)restTransport.submit(map);
        }catch (Exception e){
            logger.info(e.getMessage()+"调用任务模块失败！");
        }
        return null;
    }

    public List<Map> queryTasksAllByIds(Set<String> taskIds){
        Map<String, Object> map=new HashMap();
        map.put(Dict.IDS,new ArrayList<>(taskIds));
        map.put(Dict.REST_CODE, Dict.QUERYALLTASKSBYIDS);
        try {
            return (List<Map>)restTransport.submit(map);
        }catch (Exception e){
            logger.info(e.getMessage()+"调用任务模块失败！");
        }
        return null;
    }

    /**
     * 批量获取任务信息并封装
     * @param taskIds
     * @return
     * @throws Exception
     */
    public List<TaskEntity> getTasksInfoByIds(Set<String> taskIds) throws Exception {
        List<Map> tasks = queryTasksSimpleByIds(taskIds);
        List<TaskEntity> result = new ArrayList<>();
        for(Map task:tasks){
            TaskEntity taskEntity = new TaskEntity();
            taskEntity.setId((String) task.get(Dict.ID));
            taskEntity.setTaskName((String) task.get(Dict.NAME));
            taskEntity.setTaskStatus((String) task.get(Dict.STAGE));
            result.add(taskEntity);
        }
        return result;
    }

    /**
     * 更新任务的代码审核编号
     * @param taskIds
     * @param orderNo
     * @throws Exception
     */
    @Async
    public void updateTaskCodeOrderNo(Set<String> taskIds, String orderNo){
        Map param = new HashMap();
        param.put(Dict.REST_CODE,Dict.UPDATETASKCODEORDERNO);
        param.put(Dict.TASK_ID,taskIds);
        param.put(Dict.code_order_no,orderNo);
        try {
            restTransport.submit(param);
        }catch (Exception e){
            logger.info(e.getMessage());
        }

    }

    /**
     * 获取最晚投产的任务日期，全部投产完时才返回，否则返回null
     * @param tasks
     * @return
     */
    public String getRealProductDate(List<Map> tasks){
        String result = (String) tasks.get(0).get(Dict.FIRE_TIME);
        for(Map task : tasks){
            if(CommonUtils.isNullOrEmpty(task.get(Dict.FIRE_TIME))){
                return "";
            }
            if(TimeUtil.compareTime((String) task.get(Dict.FIRE_TIME),result)){
                result = (String) task.get(Dict.FIRE_TIME);
            }
        }
        return result;
    }
}
