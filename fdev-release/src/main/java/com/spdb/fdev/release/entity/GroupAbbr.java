package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Document(collection = "group_abbr")
public class GroupAbbr implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2134613845191011683L;
	@Indexed(unique = true)
	@Field("group_id")
	private String group_id;

	@Field("group_abbr")
	private String group_abbr;

//	@Indexed(unique = true)
	@Field("system_abbr")
	private String system_abbr;

	@Field("create_time")
	@ApiModelProperty(value="创建时间")
	private String create_time;

	@Field("update_time")
	@ApiModelProperty(value="修改时间")
	private String update_time;

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}

	public String getGroup_abbr() {
		return group_abbr;
	}

	public void setGroup_abbr(String group_abbr) {
		this.group_abbr = group_abbr;
	}

	public String getSystem_abbr() {
		return system_abbr;
	}

	public void setSystem_abbr(String system_abbr) {
		this.system_abbr = system_abbr;
	}

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
}
