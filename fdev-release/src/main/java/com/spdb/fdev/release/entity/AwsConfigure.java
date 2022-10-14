package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.io.Serializable;
import java.util.Map;

@Document(collection = "aws_configure")
@ApiModel(value = "对象存储账户配置")
public class AwsConfigure  implements Serializable {

	@Id
	@ApiModelProperty(value = "id")
	private String _id;

	@Field("group_id")
	@ApiModelProperty(value = "小组id")
	private String group_id;

	@Field("group_name")
	@ApiModelProperty(value = "小组名")
	private String group_name;

	@Field("env_config_info")
	@ApiModelProperty(value = "环境")
	private Map<String, Object> env_config_info;

	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	public Map<String, Object> getEnv_config_info() {
		return env_config_info;
	}

	public void setEnv_config_info(Map<String, Object> env_config_info) {
		this.env_config_info = env_config_info;
	}
}
