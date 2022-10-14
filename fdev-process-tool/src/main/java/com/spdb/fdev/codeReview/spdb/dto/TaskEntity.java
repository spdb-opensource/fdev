package com.spdb.fdev.codeReview.spdb.dto;

/**
 * @Author liux81
 * @DATE 2021/11/10
 */
public class TaskEntity {
    private String id;
    private String taskName;
    private String taskStatus;

    public TaskEntity() {
    }

    public TaskEntity(String id, String taskName, String taskStatus) {
        this.id = id;
        this.taskName = taskName;
        this.taskStatus = taskStatus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }
}
