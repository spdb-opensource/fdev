package com.spdb.fdev.release.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Component
@Document(collection = "operation_jnl")
@ApiModel(value = "审核记录")
public class AuditRecord implements Serializable{

	private static final long serialVersionUID = -684241233139771274L;

	@Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;
	
	@Field("release_node_name")
	@ApiModelProperty(value = "投产点名称")
	private String release_node_name;
	
	@Field("application_id")
	@ApiModelProperty(value = "应用编号")
	private String application_id;
	
	@Field("task_id")
	@ApiModelProperty(value = "任务id")
	private String task_id;

	@Field("prod_id")
	@ApiModelProperty(value = "变更id")
	private String prod_id;
	
	@Field("operation")
	@ApiModelProperty(value = "addTask;cancelRelease;changeReleaseNode...")
	private String operation;
	
	@Field("operation_type")
	@ApiModelProperty(value = "0-发起,1-确认,2-拒绝")
	private String operation_type;
	
	@Field("operator_name_en")
	@ApiModelProperty(value = "操作人英文名")
	private String operator_name_en;

	@Field("operator_name_cn")
	@ApiModelProperty(value = "操作人中文名")
	private String operator_name_cn;

	@Field("operator_email")
	@ApiModelProperty(value = "操作人邮箱")
	private String operator_email;
	
	@Field("operation_time")
	@ApiModelProperty(value = "操作时间")
	private String operation_time;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
	}

	public String getApplication_id() {
		return application_id;
	}

	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}

	public String getTask_id() {
		return task_id;
	}

	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}

	public String getProd_id() {
		return prod_id;
	}

	public void setProd_id(String prod_id) {
		this.prod_id = prod_id;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getOperation_type() {
		return operation_type;
	}

	public void setOperation_type(String operation_type) {
		this.operation_type = operation_type;
	}

	public String getOperator_name_en() {
		return operator_name_en;
	}

	public void setOperator_name_en(String operator_name_en) {
		this.operator_name_en = operator_name_en;
	}

	public String getOperator_name_cn() {
		return operator_name_cn;
	}

	public void setOperator_name_cn(String operator_name_cn) {
		this.operator_name_cn = operator_name_cn;
	}

	public String getOperator_email() {
		return operator_email;
	}

	public void setOperator_email(String operator_email) {
		this.operator_email = operator_email;
	}

	public String getOperation_time() {
		return operation_time;
	}

	public void setOperation_time(String operation_time) {
		this.operation_time = operation_time;
	}
	
	
}
