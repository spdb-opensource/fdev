package com.spdb.fdev.spdb.service.impl;

import com.spdb.fdev.base.dict.Dict;
import com.spdb.fdev.spdb.service.ITaskService;
import com.spdb.fdev.transport.RestTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class TaskServiceImpl implements ITaskService {

    @Autowired
    private RestTransport restTransport;

    @Override
    public void updateTaskSonarTime(String application_name, String branch_name, String time) throws Exception {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("web_name_en", application_name);
        map.put("feature_id", branch_name);
        map.put("scan_time", time);
        map.put(Dict.REST_CODE, "updateTaskSonarId");
        this.restTransport.submit(map);
    }

    @Override
    public void updateTaskSonarId(String application_name, String branch_name, String sonar_id) throws Exception {
        HashMap<Object, Object> map = new HashMap<>();
        map.put("web_name_en", application_name);
        map.put("feature_id", branch_name);
        map.put("sonar_id", sonar_id);
        map.put(Dict.REST_CODE, "updateTaskSonarId");
        this.restTransport.submit(map);
    }

}
