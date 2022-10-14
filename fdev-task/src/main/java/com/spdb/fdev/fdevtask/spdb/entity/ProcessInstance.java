package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevtask.spdb.entity.ComponentStatus;
import com.spdb.fdev.fdevtask.spdb.entity.ProcessStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

/**
 * @program: fdev-process
 * @description:
 * @author: c-gaoys
 * @create: 2021-02-01 10:16
 **/
@Document(collection = "process_instance")
@ApiModel(value = "流程实例")
public class ProcessInstance {
    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "流程实例id")
    private String id;

    @Field("processId")
    @ApiModelProperty(value = "流程ID")
    private String processId;

    @Field("taskId")
    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @Field("processStatusList")
    @ApiModelProperty(value = "一个流程中的所有状态")
    private List<ProcessStatus> processStatusList;

    @Field("taskStatusId")
    @ApiModelProperty(value = "当前任务状态")
    private String taskStatusId;

    @Field("componentStatusList")
    @ApiModelProperty(value = "组件执行状态")
    private List<ComponentStatus> componentStatusList;

    @Field("taskHistoryStatus")
    @ApiModelProperty(value = "任务状态变化记录{id:状态id，name:状态名，dateTime:进入该状态时间，user:进入该状态操作人id}")
    private List<Map<String,String>> taskHistoryStatus;

    public ProcessInstance() {
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public List<ProcessStatus> getProcessStatusList() {
        return processStatusList;
    }

    public void setProcessStatusList(List<ProcessStatus> processStatusList) {
        this.processStatusList = processStatusList;
    }

    public String getTaskStatusId() {
        return taskStatusId;
    }

    public void setTaskStatusId(String taskStatusId) {
        this.taskStatusId = taskStatusId;
    }

    public List<ComponentStatus> getComponentStatusList() {
        return componentStatusList;
    }

    public void setComponentStatusList(List<ComponentStatus> componentStatusList) {
        this.componentStatusList = componentStatusList;
    }

    public List<Map<String, String>> getTaskHistoryStatus() {
        return taskHistoryStatus;
    }

    public void setTaskHistoryStatus(List<Map<String, String>> taskHistoryStatus) {
        this.taskHistoryStatus = taskHistoryStatus;
    }
}
