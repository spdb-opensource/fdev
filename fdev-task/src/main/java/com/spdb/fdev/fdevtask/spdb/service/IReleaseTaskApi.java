package com.spdb.fdev.fdevtask.spdb.service;


import java.util.Map;

import com.spdb.fdev.fdevtask.spdb.entity.AppDeployReview;

public interface IReleaseTaskApi {

    public Map deleteTask(String taskId) throws Exception;

    public Map queryDetailByTaskId(Map param) throws Exception;

    public Map queryDetailByTaskId(String taskId) throws Exception;

    public Map queryTaskByInterval(String groupId, String startDate, String endDate) throws Exception;

    public Map queryDetailByTaskId(String taskId,String type) throws Exception;

    public Map addRqrmntInfo(Map<String,Object> params) throws Exception;

    Map updateRqrmntInfoReview(Map<String,Object> params);

    Map deleteRqrmntInfoTask(Map<String,Object> params);

    void dbReviewUpload(String id) throws Exception;

    Map queryRqrmntInfoByTaskNo(String id );

    void confirmRqrmntInfoTag(String confirmFileDate, String task_id );
    
    void deploy(Map<String,Object> map);
}
