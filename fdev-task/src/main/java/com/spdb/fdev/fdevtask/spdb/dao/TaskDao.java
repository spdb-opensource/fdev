package com.spdb.fdev.fdevtask.spdb.dao;



import com.mongodb.bulk.BulkWriteResult;
import com.spdb.fdev.fdevtask.spdb.entity.Task;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

public interface TaskDao {

    public List<Task> query(Task task) throws Exception;

    public Task save(Task task) throws Exception;

    public  Map getTaskList(String userId, Map params) throws Exception;

    public List<Task> isRepeat(String fieldName) throws Exception;

    public Task updateTask(Task task) throws Exception;

    public Task updateTaskInfo(Task task) throws Exception;

    public List<Task>  getProcessList(String userId,Map request) throws Exception;

    void updateEndTime(String taskId,String planEndDate) throws Exception;

    Task deleteTask(String id) throws Exception;

    public List<Task> queryIds(List<String> ids) throws Exception;

    public Map queryTaskByApplicationId(String applicationId,Integer type,int pageNum,int page,String name) throws Exception;

    public Map queryTaskByFdevUnitId(String fdevUnitId, int pageNum, int page,Integer delete) throws Exception;

    List<Task> querTaskListByImplUnitIds( List<String> implUnitIds,Integer delete) throws Exception;

    List<Task> queryTaskByVersionIds(List<String> versionIds,Integer mountStatus,Integer delete) throws Exception;

    List<String> updateMountStatus(Integer mountStatus,String versionId,List<Map> result) throws Exception;

    Integer queryNotEndTask(String processId,String groupId);

    List<Task> queryByGroupId(List groupIds);

    long queryTaskNum();

    <T> List<T> queryByQuery(Query query, Class<T> clazz);

    BulkWriteResult updateMountSameFBConfirms(List<Map> maps);
}
