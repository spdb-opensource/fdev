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
@Document(collection = "prod_note")
@ApiModel(value = "发布说明记录表")
public class ProdNote implements Serializable {

	private static final long serialVersionUID = 3532629789338313598L;
	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;

	@Field("note_id")
	@ApiModelProperty(value = "发布说明id")
	private String note_id;

	@Field("release_node_name")
	@ApiModelProperty(value = "投产窗口名称")
	private String release_node_name;

	@Field("date")
	@ApiModelProperty(value = "投产日期")
	private String date;
	
	@Field("version")
	@ApiModelProperty(value = "变更版本")
	private String version;

	@Field("type")
	@ApiModelProperty(value = "变更类型")
	private String type;// gray（灰度）、proc（生产）

	@Field("launcher")
	@ApiModelProperty(value = "变更发起人")
	private String launcher;

	@Field("launcher_name_cn")
	@ApiModelProperty(value = "变更发起人中文名")
	private String launcher_name_cn;

	@Field("create_time")
	@ApiModelProperty(value = "变更创建时间")
	private String create_time;

	@Field("owner_system")
	@ApiModelProperty(value = "系统编号")
	private String owner_system;

	@Field("owner_system_name")
	@ApiModelProperty(value = "系统名称")
	private String owner_system_name;
	
	@Field("owner_groupId")
	@ApiModelProperty(value = "小组id")
	private String owner_groupId;
	
	@Field("plan_time")
	@ApiModelProperty(value = "变更批次时间")
	private String plan_time;

	@Field("image_deliver_type")
	private String image_deliver_type;//是否自动化发布（1是0否）
	
	@Field("lock_flag")
	@ApiModelProperty(value = "锁定状态")
	private String lock_flag;//0-锁定,1-未锁定
	
	@Field("lock_people")
	@ApiModelProperty(value = "锁定人")
	private String lock_people;

	@Field("lock_name_cn")
	@ApiModelProperty(value = "锁定人中文名")
	private String lock_name_cn;

	@Field("release_note_name")
	@ApiModelProperty(value = "发布说明名称")
	private String release_note_name;
	
	@Field("namespace")
	@ApiModelProperty(value = "环境类型")
	private String namespace;// 1-业务 2-网银
	
	@Field("template_id")
	@ApiModelProperty(value = "变更模板id空")
	private String template_id;
	
	@Field("note_download_url")
	@ApiModelProperty(value = "发布说明minio文件下载地址")
	private String note_download_url;

	@Field("note_batch")
	@ApiModelProperty(value = "发布说明批次")
	private String note_batch;
	
	@Field("leaseholder")
	@ApiModelProperty(value = "租户")
	private String leaseholder;
	
	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getNote_id() {
		return note_id;
	}

	public void setNote_id(String note_id) {
		this.note_id = note_id;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLauncher() {
		return launcher;
	}

	public void setLauncher(String launcher) {
		this.launcher = launcher;
	}

	public String getLauncher_name_cn() {
		return launcher_name_cn;
	}

	public void setLauncher_name_cn(String launcher_name_cn) {
		this.launcher_name_cn = launcher_name_cn;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getOwner_system() {
		return owner_system;
	}

	public void setOwner_system(String owner_system) {
		this.owner_system = owner_system;
	}

	public String getOwner_system_name() {
		return owner_system_name;
	}

	public void setOwner_system_name(String owner_system_name) {
		this.owner_system_name = owner_system_name;
	}

	public String getOwner_groupId() {
		return owner_groupId;
	}

	public void setOwner_groupId(String owner_groupId) {
		this.owner_groupId = owner_groupId;
	}

	public String getPlan_time() {
		return plan_time;
	}

	public void setPlan_time(String plan_time) {
		this.plan_time = plan_time;
	}

	public String getImage_deliver_type() {
		return image_deliver_type;
	}

	public void setImage_deliver_type(String image_deliver_type) {
		this.image_deliver_type = image_deliver_type;
	}

	public String getLock_flag() {
		return lock_flag;
	}

	public void setLock_flag(String lock_flag) {
		this.lock_flag = lock_flag;
	}

	public String getLock_people() {
		return lock_people;
	}

	public void setLock_people(String lock_people) {
		this.lock_people = lock_people;
	}

	public String getLock_name_cn() {
		return lock_name_cn;
	}

	public void setLock_name_cn(String lock_name_cn) {
		this.lock_name_cn = lock_name_cn;
	}

	public String getRelease_note_name() {
		return release_note_name;
	}

	public void setRelease_note_name(String release_note_name) {
		this.release_note_name = release_note_name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(String template_id) {
		this.template_id = template_id;
	}

	public String getNote_download_url() {
		return note_download_url;
	}

	public void setNote_download_url(String note_download_url) {
		this.note_download_url = note_download_url;
	}

	public String getNote_batch() {
		return note_batch;
	}

	public void setNote_batch(String note_batch) {
		this.note_batch = note_batch;
	}

	public String getLeaseholder() {
		return leaseholder;
	}

	public void setLeaseholder(String leaseholder) {
		this.leaseholder = leaseholder;
	}

}
