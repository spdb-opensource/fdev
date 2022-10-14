package com.spdb.fdev.release.entity;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Document(collection = "release_role")
@ApiModel(value = "投产模块角色")
public class RoleEntity implements Serializable{

	private static final long serialVersionUID = -8284271917149415526L;

	@Field("name")
	@ApiModelProperty(value = "release_manager")
	private String name;
	
	@Field("name_cn")
	@ApiModelProperty(value = "投产负责人")
	private String nameCn;
	
	@Field("description")
	@ApiModelProperty(value = "负责某个投产点的介质准备")
	private String description;
	
	@Field("permissions")
	@ApiModelProperty(value = "任务列表")
	private String permissions;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameCn() {
		return nameCn;
	}

	public void setNameCn(String nameCn) {
		this.nameCn = nameCn;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPermissions() {
		return permissions;
	}

	public void setPermissions(String permissions) {
		this.permissions = permissions;
	}
	

}
