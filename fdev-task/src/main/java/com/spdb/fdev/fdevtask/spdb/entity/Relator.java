package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@ApiModel(value="相关人员信息")
public class Relator {
    @Field("rid")
    private String rid;
    @ApiModelProperty(value="相关人员id")
    @Field("relatorId")
    private String relatorId;
    @ApiModelProperty(value="发起人id")
    @Field("currentId")
    private String currentId;
    @ApiModelProperty(value="相关人员姓名")
    @Field("relatorName")
    private String relatorName;
    @ApiModelProperty(value="发起人姓名")
    @Field("currentName")
    private String currentName;
    @ApiModelProperty(value="任务开始时间")
    @Field("taskStartTime")
    private String taskStartTime;
    @ApiModelProperty(value="任务结束时间")
    @Field("taskEndTime")
    private String taskEndTime;
    @ApiModelProperty(value="任务状态")
    @Field("taskStatus")
    private String taskStatus;
    @ApiModelProperty(value="文件名字")
    @Field("fileName")
    private String fileName;

    @ApiModelProperty(value="文件管理")
    /**
     * map key：path ,createTime  ,fileName
     */
    @Field("files")
    private List<Map<String,String>> files;

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getRelatorId() {
        return relatorId;
    }

    public void setRelatorId(String relatorId) {
        this.relatorId = relatorId;
    }

    public String getCurrentId() {
        return currentId;
    }

    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }

    public String getRelatorName() {
        return relatorName;
    }

    public void setRelatorName(String relatorName) {
        this.relatorName = relatorName;
    }

    public String getCurrentName() {
        return currentName;
    }

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    public String getTaskStartTime() {
        return taskStartTime;
    }

    public void setTaskStartTime(String taskStartTime) {
        this.taskStartTime = taskStartTime;
    }

    public String getTaskEndTime() {
        return taskEndTime;
    }

    public void setTaskEndTime(String taskEndTime) {
        this.taskEndTime = taskEndTime;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public List<Map<String, String>> getFiles() {
        return files;
    }

    public void setFiles(List<Map<String, String>> files) {
        this.files = files;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "Relator{" +
                "rid='" + rid + '\'' +
                ", relatorId='" + relatorId + '\'' +
                ", currentId='" + currentId + '\'' +
                ", relatorName='" + relatorName + '\'' +
                ", currentName='" + currentName + '\'' +
                ", taskStartTime='" + taskStartTime + '\'' +
                ", taskEndTime='" + taskEndTime + '\'' +
                ", taskStatus=" + taskStatus +
                ", files=" + files +
                '}';
    }
}
