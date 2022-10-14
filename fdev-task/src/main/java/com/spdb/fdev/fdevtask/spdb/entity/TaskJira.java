package com.spdb.fdev.fdevtask.spdb.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModelProperty;

@Document(collection = "task_jira")
public class TaskJira {
	
	@Id
    private Object _id;
	
	@Field("id")
    @ApiModelProperty(value = "ID")
    private String id;

    @ApiModelProperty("缺陷ID")
    @Field("jira_id")
    private String jira_id;

    @ApiModelProperty("缺陷key")
    @Field("jira_key")
    private String jira_key;

    @ApiModelProperty("任务ID")
    @Field("task_id")
    private String task_id;

    @ApiModelProperty("任务名称")
    @Field("task_name")
    private String task_name;
    
    @ApiModelProperty("需求ID")
    @Field("demand_id")
    private String demand_id;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJira_id() {
		return jira_id;
	}

	public void setJira_id(String jira_id) {
		this.jira_id = jira_id;
	}

	public String getJira_key() {
		return jira_key;
	}

	public void setJira_key(String jira_key) {
		this.jira_key = jira_key;
	}

	public String getTask_id() {
		return task_id;
	}

	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}

	public String getTask_name() {
		return task_name;
	}

	public void setTask_name(String task_name) {
		this.task_name = task_name;
	}
	
	public String getDemand_id() {
		return demand_id;
	}

	public void setDemand_id(String demand_id) {
		this.demand_id = demand_id;
	}

	public TaskJira(){};

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }	

}
