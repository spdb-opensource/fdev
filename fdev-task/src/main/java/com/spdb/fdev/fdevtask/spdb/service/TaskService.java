package com.spdb.fdev.fdevtask.spdb.service;

import com.mongodb.bulk.BulkWriteResult;
import com.spdb.fdev.fdevtask.spdb.entity.EnumData;
import com.spdb.fdev.fdevtask.spdb.entity.Task;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * @program: fdev-process
 * @description:
 * @author: c-jiangl2
 * @create: 2021-01-25 13:28
 **/
public interface TaskService {

    public Task createTask(Map request) throws Exception;

    public Map updateTask(Map request) throws Exception;

    public Map getTaskList(Map params) throws Exception;

    public Map getTaskById(Map request) throws Exception;

    public Map getTaskDetail(Map request) throws Exception;

    Long uploadFile(String taskId,List<Map<String, String>> doc, MultipartFile[] file) throws Exception;

    Long updateTaskDoc(String taskId, List<Map<String, String>> doc) throws Exception;

    Map queryDocDetail(String taskId) throws Exception;

    List<Map> getProcessList(Map request)  throws Exception;

    void updateEndTime(Map request)  throws Exception;

    void changeTaskStatus(Map request)  throws Exception;

    List<String> getImplUnitNoByIds(Map request)  throws Exception;

    Map queryTaskByApplicationId(Map request) throws Exception;

    Map<String,Object> queryTaskByFdevUnitId(Map request) throws Exception;

    List<Task> queryTaskByRequirementId(Map request) throws Exception;

    void deleteTask(Map request) throws Exception;

    List<Task> queryTaskByUserId(Map request) throws Exception;

    Task updateTaskInner(Map request) throws Exception;

    List<Task> querTaskListByImplUnitIds(Map request) throws Exception;

    List<Map> queryTaskByVersionIds(Map request) throws Exception;

    List<String> updateMountStatus(Map request) throws Exception;

    public List<Task> getTaskByTask(Map request) throws Exception;

    public Map getTaskByRequirementId(Map params) throws Exception;

    List<Map> getProcessListByRequirementId(List<Task> taskList)  throws Exception;

    public List<Map> getTaskByIds(Map request) throws Exception;

    Integer queryNotEndTask (String processId, String groupId);

    List<Map> getCompleteTaskByImplIds(Map request) throws Exception;

    BulkWriteResult updateMountSameFBConfirms(Map request);
}
