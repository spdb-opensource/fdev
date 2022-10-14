package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Document(collection = "prod_template")
@ApiModel(value = "变更模板表")
public class ProdTemplate implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5650728343854020916L;

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;

	@Field("id")
	@Indexed(unique = true)
	private String  id;
	
	@Field("temp_name")
	@ApiModelProperty(value = "模版名")
	private String temp_name;

	@Field("owner_system")
	@ApiModelProperty(value = "所属系统")
	private String owner_system;

	@Field("template_type")
	@ApiModelProperty(value = "模板类型（gray灰度、proc生产）")
	private String template_type;

	@Field("owner_app")
	@ApiModelProperty(value = "所属应用ID")
	private String owner_app;

	@Field("temp_giturl")
	@ApiModelProperty(value = "模板excel git仓库地址")
	private String temp_giturl;

	@Field("create_time")
	@ApiModelProperty(value="创建时间")
	private String create_time;

	@Field("update_user")
	@ApiModelProperty(value = "更新人userid")
	private String update_user;

	@Field("update_time")
	private String update_time;

	@Field("file_md5")
	private String file_md5;
	
	@Field("status")
	private String status;

	@Field("owner_group")
	private String owner_group;

	public String getFile_md5() {
		return file_md5;
	}

	public void setFile_md5(String file_md5) {
		this.file_md5 = file_md5;
	}

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

	public String getTemp_name() {
		return temp_name;
	}

	public void setTemp_name(String temp_name) {
		this.temp_name = temp_name;
	}

	public String getOwner_system() {
		return owner_system;
	}

	public void setOwner_system(String owner_system) {
		this.owner_system = owner_system;
	}

	public String getTemplate_type() {
		return template_type;
	}

	public void setTemplate_type(String template_type) {
		this.template_type = template_type;
	}

	public String getOwner_app() {
		return owner_app;
	}

	public void setOwner_app(String owner_app) {
		this.owner_app = owner_app;
	}


	public String getTemp_giturl() {
		return temp_giturl;
	}

	public void setTemp_giturl(String temp_giturl) {
		this.temp_giturl = temp_giturl;
	}

	public String getUpdate_user() {
		return update_user;
	}

	public void setUpdate_user(String update_user) {
		this.update_user = update_user;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getOwner_group() {
		return owner_group;
	}

	public void setOwner_group(String owner_group) {
		this.owner_group = owner_group;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}
}
