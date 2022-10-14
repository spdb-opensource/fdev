package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;

@ApiModel(value="任务关联子项")
public class TaskReviewChild {
	
	@ApiModelProperty(value="任务关联子项ID")
	@Field("cid")
	private String id;
	
	@ApiModelProperty(value="任务关联子项名称")
	private String name;
	
	@ApiModelProperty(value="任务关联子项审核结果")
	private Boolean audit;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getAudit() {
		return audit;
	}

	public void setAudit(Boolean audit) {
		this.audit = audit;
	}
}
