package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "todo_list")
public class ToDoList {

    @Field("id")
    @Id
    private String id;

    @ApiModelProperty("代办负责人")
    @Field("user_ids")
    private String[] user_ids;

    @ApiModelProperty("所属模块")
    @Field("module")
    private String module;

    @Field("description")
    private String description;

    @ApiModelProperty("链接")
    @Field("link")
    private String link;

    @ApiModelProperty("代办类型")
    @Field("type")
    private String type;

    @ApiModelProperty("目标id")
    @Field("target_id")
    private String target_id;

    @ApiModelProperty("创建人id")
    @Field("create_user_id")
    private String create_user_id;

    @Field("create_date")
    private String create_date;

    @Field("status")
    private String status;

    @Field("merge_id")
    private String merge_id;

    @Field("project_id")
    private String project_id;

    @Field("task_id")
    private String task_id;

    @Field("executor_id")
    private String executor_id;

    @Field("executor_name")
    private String executor_name;

    @Field("executor_time")
    private String executor_time;

    public ToDoList(){};

    public ToDoList(String merge_id, String project_id) {
        this.merge_id = merge_id;
        this.project_id = project_id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getUser_ids() {
        return user_ids;
    }

    public void setUser_ids(String[] user_ids) {
        this.user_ids = user_ids;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget_id() {
        return target_id;
    }

    public void setTarget_id(String target_id) {
        this.target_id = target_id;
    }

    public String getCreate_user_id() {
        return create_user_id;
    }

    public void setCreate_user_id(String create_user_id) {
        this.create_user_id = create_user_id;
    }

    public String getMerge_id() {
        return merge_id;
    }

    public void setMerge_id(String merge_id) {
        this.merge_id = merge_id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public String getExecutor_id() {
        return executor_id;
    }

    public void setExecutor_id(String executor_id) {
        this.executor_id = executor_id;
    }

    public String getExecutor_time() {
        return executor_time;
    }

    public void setExecutor_time(String executor_time) {
        this.executor_time = executor_time;
    }

    public String getExecutor_name() {
        return executor_name;
    }

    public void setExecutor_name(String executor_name) {
        this.executor_name = executor_name;
    }
}
