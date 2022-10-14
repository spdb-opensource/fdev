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
@Document(collection = "batch_task_info")
@ApiModel(value = "批量任务信息表")
public class BatchTaskInfo implements Serializable {

	private static final long serialVersionUID = 3532629789338317681L;
	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;
	
	@Field("id")
	@ApiModelProperty(value = "批量任务id")
	private String id;

	@Field("release_node_name")
	@ApiModelProperty(value = "投产窗口名称")
	private String release_node_name;

	@Field("application_id")
	@ApiModelProperty(value = "应用id")
	private String application_id;
	
	@Field("application_name_en")
	@ApiModelProperty(value = "应用英文名")
	private String application_name_en;

	@Field("application_name_cn")
	@ApiModelProperty(value = "应用中文名")
	private String application_name_cn;

	@Field("type")
	@ApiModelProperty(value = "任务类型")
	private String type;

	@Field("executorId")
	@ApiModelProperty(value = "服务注册中心注册的名称")
	private String executorId;

	@Field("transName")
	@ApiModelProperty(value = "交易名")
	private String transName;

	@Field("jobGroup")
	@ApiModelProperty(value = "组别")
	private String jobGroup;

	@Field("description")
	@ApiModelProperty(value = "交易中文名")
	private String description;
	
	@Field("cronExpression")
	@ApiModelProperty(value = "触发规则")
	private String cronExpression;
	
	@Field("misfireInstr")
	@ApiModelProperty(value = "miss补跑策略")
	private String misfireInstr;
	
	@Field("fireTime")
	@ApiModelProperty(value = "一次性任务触发时间")
	private String fireTime;

	@Field("note_id")
	@ApiModelProperty(value = "发布说明id")
	private String note_id;
	
	@Field("prod_id")
	@ApiModelProperty(value = "变更id")
	private String prod_id;
	
	@Field("user_name_cn")
	@ApiModelProperty(value = "创建人")
	private String user_name_cn;
	
	@Field("create_time")
	@ApiModelProperty(value = "创建时间")
	private String create_time;
	
	@Field("batchInfo")
	@ApiModelProperty(value = "批量语句")
	private String batchInfo;
	
	@Field("batch_no")
	@ApiModelProperty(value = "执行序号")
	private String batch_no;
	
	@Field("user_name_phone")
	@ApiModelProperty(value = "创建人联系方式")
	private String user_name_phone;

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

	public String getApplication_name_en() {
		return application_name_en;
	}

	public void setApplication_name_en(String application_name_en) {
		this.application_name_en = application_name_en;
	}

	public String getApplication_name_cn() {
		return application_name_cn;
	}

	public void setApplication_name_cn(String application_name_cn) {
		this.application_name_cn = application_name_cn;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExecutorId() {
		return executorId;
	}

	public void setExecutorId(String executorId) {
		this.executorId = executorId;
	}

	public String getTransName() {
		return transName;
	}

	public void setTransName(String transName) {
		this.transName = transName;
	}

	public String getJobGroup() {
		return jobGroup;
	}

	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public String getMisfireInstr() {
		return misfireInstr;
	}

	public void setMisfireInstr(String misfireInstr) {
		this.misfireInstr = misfireInstr;
	}

	public String getFireTime() {
		return fireTime;
	}

	public void setFireTime(String fireTime) {
		this.fireTime = fireTime;
	}

	public String getNote_id() {
		return note_id;
	}

	public void setNote_id(String note_id) {
		this.note_id = note_id;
	}

	public String getProd_id() {
		return prod_id;
	}

	public void setProd_id(String prod_id) {
		this.prod_id = prod_id;
	}

	public String getUser_name_cn() {
		return user_name_cn;
	}

	public void setUser_name_cn(String user_name_cn) {
		this.user_name_cn = user_name_cn;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getBatchInfo() {
		return batchInfo;
	}

	public void setBatchInfo(String batchInfo) {
		this.batchInfo = batchInfo;
	}

	public String getBatch_no() {
		return batch_no;
	}

	public void setBatch_no(String batch_no) {
		this.batch_no = batch_no;
	}

	public String getUser_name_phone() {
		return user_name_phone;
	}

	public void setUser_name_phone(String user_name_phone) {
		this.user_name_phone = user_name_phone;
	}
}
