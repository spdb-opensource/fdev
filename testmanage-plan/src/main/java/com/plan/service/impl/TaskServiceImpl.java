package com.plan.service.impl;

import com.plan.dict.Dict;
import com.plan.service.TaskService;
import com.test.testmanagecommon.transport.RestTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskServiceImpl implements TaskService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RestTransport restTransport;

    @Override
    public Map<String, Object> queryTaskDetail(String task_id) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put(Dict.ID, task_id);
        map.put(Dict.REST_CODE, "fdev.ftask.queryTaskDetail");
        return (Map<String, Object>) restTransport.submitSourceBack(map);
    }

    @Override
    public List<Map> queryInfoByTaskNos(List<String> TaskNos) throws Exception {
        Map sendData = new HashMap();
        sendData.put(Dict.IDS, TaskNos);
        sendData.put(Dict.REST_CODE, "fdev.task.ids.query");
        List<Map> tasks = (List<Map>) restTransport.submitSourceBack(sendData);
        return  tasks;
    }

    /**
     * 新fev任务
     * @param id
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, Object> getNewTaskById(String id) throws Exception {
        Map send = new HashMap();
        send.put(Dict.ID, id);
        send.put(Dict.REST_CODE, "getTaskById");
        try {
            return  (Map<String, Object>) restTransport.submitSourceBack(send);
        } catch (Exception e) {
            logger.error("fail to get new Task info");
        }
        return null;
    }

    @Override
    public Map<String, Object> queryTaskInfo(String taskNo, List<String> fields) {
        Map<String, Object> param = new HashMap();
        param.put(Dict.ID, taskNo);
        param.put(Dict.FIELDS, fields);
        param.put(Dict.REST_CODE, "queryTaskInfoById");
        try {
            return  (Map<String, Object>) restTransport.submitSourceBack(param);
        } catch (Exception e) {
            logger.error("fail to get Task info, taskNo:{}", taskNo);
        }
        return new HashMap<>();
    }


}
