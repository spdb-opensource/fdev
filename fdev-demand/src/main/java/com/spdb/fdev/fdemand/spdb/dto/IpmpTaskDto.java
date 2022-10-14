package com.spdb.fdev.fdemand.spdb.dto;

public class IpmpTaskDto {
    private String id;

    /**
     * 任务集编号
     */
    private String taskNo;

    /**
     * 任务集名称
     */
    private String taskName;

    /**
     * 所属小组id
     */
    private String groupId;
    /**
     * 所属小组名称
     */
    private String groupName;

    /**
     * 实施单元编号
     */
    private String ipmpImplementUnitNo;

    /**
     * 实施单元实施内容
     */
    private String implementUnitContent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskNo() {
        return taskNo;
    }

    public void setTaskNo(String taskNo) {
        this.taskNo = taskNo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getIpmpImplementUnitNo() {
        return ipmpImplementUnitNo;
    }

    public void setIpmpImplementUnitNo(String ipmpImplementUnitNo) {
        this.ipmpImplementUnitNo = ipmpImplementUnitNo;
    }

    public String getImplementUnitContent() {
        return implementUnitContent;
    }

    public void setImplementUnitContent(String implementUnitContent) {
        this.implementUnitContent = implementUnitContent;
    }
}
