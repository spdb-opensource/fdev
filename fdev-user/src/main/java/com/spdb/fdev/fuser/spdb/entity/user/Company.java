package com.spdb.fdev.fuser.spdb.entity.user;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.annotations.ApiModelProperty;

@Document(collection = "company")
@ApiModel(value="公司")
public class Company implements Serializable {

	private static final long serialVersionUID = -6785268527663229814L;
	@Id
	@JsonIgnore
	private ObjectId _id;

	@Field("id")
	@ApiModelProperty(value="公司ID")
	private String id;
	
	@Field("name")
	@ApiModelProperty(value="公司名称")
	private String name;
	
	@ApiModelProperty(value="公司人数")
	private Integer count;
	
	@Field("status")
	@ApiModelProperty(value="公司状态, 0是废弃, 1是使用")
	private String status;

	@Field("createTime")
	@ApiModelProperty(value="创建时间")
	private String createTime;

	@Field("deleteTime")
	@ApiModelProperty(value="删除时间")
	private String deleteTime;
	
	public Company() {
		
	}

	public Company(String name, Integer count, String status, String createTime, String deleteTime) {
		this.name = name;
		this.count = count;
		this.status = status;
		this.createTime = createTime;
		this.deleteTime = deleteTime;
	}

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

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public ObjectId get_id() {
		return _id;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getDeleteTime() {
		return deleteTime;
	}

	public void setDeleteTime(String deleteTime) {
		this.deleteTime = deleteTime;
	}

	public void initId() {
		ObjectId temp = new ObjectId();
		this.id=temp.toString();
		this._id=temp;
	}
	
}
