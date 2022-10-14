package com.spdb.fdev.fuser.spdb.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fuser.spdb.entity.user.Group;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;


@ApiModel(value="组织结构页面的用户列表字段")
public class UserListInGroupPage implements Serializable {

	private static final long serialVersionUID = -5382360368973552125L;

	@ApiModelProperty(value="用户ID")
	private String id;

	@ApiModelProperty(value="用户英文名称")
	private String user_name_en;//用户英文名称  
	
	@ApiModelProperty(value="用户中文名称")
	private String user_name_cn;//用户中文名称
	
	@ApiModelProperty(value="用户邮箱")
	private String email;//用户邮箱
	
	@ApiModelProperty(value="所属小组ID")
	private String group_id;//group{ id:"id" , groupName : "name" }

	@ApiModelProperty(value="所属小组名称")
	private String name;

	@ApiModelProperty(value="所属小组全名称")
	private String fullName;
	
	@ApiModelProperty(value="手机号")
	private String telephone;//手机号

	public UserListInGroupPage() {
		
	}

	public UserListInGroupPage(String id, String user_name_en, String user_name_cn, String email, String group_id, String name, String fullName, String telephone) {
		this.id = id;
		this.user_name_en = user_name_en;
		this.user_name_cn = user_name_cn;
		this.email = email;
		this.group_id = group_id;
		this.name = name;
		this.fullName = fullName;
		this.telephone = telephone;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getUser_name_en() {
		return user_name_en;
	}

	public void setUser_name_en(String user_name_en) {
		this.user_name_en = user_name_en;
	}

	public String getUser_name_cn() {
		return user_name_cn;
	}

	public void setUser_name_cn(String user_name_cn) {
		this.user_name_cn = user_name_cn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	@Override
	public String toString() {
		return "UserListInGroupPage{" +
				"id='" + id + '\'' +
				", user_name_en='" + user_name_en + '\'' +
				", user_name_cn='" + user_name_cn + '\'' +
				", email='" + email + '\'' +
				", group_id='" + group_id + '\'' +
				", name='" + name + '\'' +
				", fullName='" + fullName + '\'' +
				", telephone='" + telephone + '\'' +
				'}';
	}
}
