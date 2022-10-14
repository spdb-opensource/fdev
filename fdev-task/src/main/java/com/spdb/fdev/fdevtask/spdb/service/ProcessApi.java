package com.spdb.fdev.fdevtask.spdb.service;

import com.spdb.fdev.fdevtask.spdb.entity.Component;
import com.spdb.fdev.fdevtask.spdb.entity.ModelExt;

import java.util.List;
import java.util.Map;

public interface ProcessApi {

    Object getProcessDetail(String processId) throws Exception;

    Object updateValue(List<ModelExt> modelExts) throws Exception;

    void createProcessInstance(String taskId,String processId) throws Exception;

    Map getFieldAndValue(String model, String modelId, String teamId,String lable) throws Exception;

    Map queryInstanceByTaskId(String taskId);

    List<Component> queryComponent(List<String> id) throws Exception;

    Map getInstanceStatus(String task_id, String statusId ) throws Exception;

    void changeTaskStatus(Map request) throws Exception;

    List<Map> queryComponentByIds(List<String> ids);

    void createFeature(String taskId,String applicationId,String branchName,String requirmentNo,String createFrom,List<String> assigneeList) throws Exception;

    List<Map> queryInstanceByTaskIds(List<String> taskIds);

    List<Map> mountVersionCallBack(Integer mountStatus,List<String> taskId,String versionId) throws Exception;

    List<Map> queryComponentList() throws Exception;
}
