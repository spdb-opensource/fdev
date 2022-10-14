package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
@Document(collection = "release_tasks")
@ApiModel(value = "投产任务")
@CompoundIndexes({
		@CompoundIndex(name = "rls_task_idx", def = "{'release_node_name': 1, 'task_id': 1}", unique = true)
})
public class ReleaseTask implements Serializable{

	private static final long serialVersionUID = 6892686649637282595L;
	
	@Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;
	
	@Field("task_id")
	@ApiModelProperty(value = "任务id")
	private String task_id;
	
	@Field("task_status")
	@ApiModelProperty(value = "任务审核状态")
	private String task_status;
	
	@Field("release_node_name")
	@ApiModelProperty(value = "投产点名称")
	private String release_node_name;
	
	@Field("application_id")
	@ApiModelProperty(value = "应用编号")
	private String application_id;
	
	@Field("reject_reason")
	@ApiModelProperty(value = "拒接理由")
	private String reject_reason;

	@Field("type")
	@ApiModelProperty(value = "投产窗口类型")
	private String type;

	@Field("create_time")
	@ApiModelProperty(value = "创建时间")
	private String create_time;

	@Field("update_time")
	@ApiModelProperty(value = "更新时间")
	private String update_time;

	@Field("rqrmnt_no")
	@ApiModelProperty(value = "需求id")
	private String rqrmnt_no;

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public String getTask_id() {
		return task_id;
	}

	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}


	public String getTask_status() {
		return task_status;
	}

	public void setTask_status(String task_status) {
		this.task_status = task_status;
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

	public String getReject_reason() {
		return reject_reason;
	}

	public void setReject_reason(String reject_reason) {
		this.reject_reason = reject_reason;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRqrmnt_no() { return rqrmnt_no; }

	public void setRqrmnt_no(String rqrmnt_no) {this.rqrmnt_no = rqrmnt_no; }
}
