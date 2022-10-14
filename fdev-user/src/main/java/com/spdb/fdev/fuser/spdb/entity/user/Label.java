package com.spdb.fdev.fuser.spdb.entity.user;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Document(collection = "label")
@ApiModel(value="标签")
public class Label implements Serializable{

	private static final long serialVersionUID = 5051112304025031810L;

	@Id
	@JsonIgnore
	private Object _id;

	@Field("id")
	@ApiModelProperty(value="标签ID")
	private String id;
	
	@Field("name")
	@ApiModelProperty(value="标签名称")
	private String name;
	
	@Field("count")
	@ApiModelProperty(value="标签热度")
	private Integer count;//热度  有多少人使用过该标签，每次新增用户 +1  ，修改删除用户不动

	@Field("status")
	@ApiModelProperty(value="标签状态, 0是废弃, 1是使用")
	private String status;
	
	public Label() {}
	
	public Label(String id, String name, Integer count, String status) {
		this.id = id;
		this.name = name;
		this.count = count;
		this.status = status;
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

	public Object get_id() {
		return _id;
	}

	public void set_id(Object _id) {
		this._id = _id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	//存数据时，初始化_id和id
	public void initId() {
		ObjectId temp = new ObjectId();
		this._id = temp;
		this.id = temp.toString();
	}


}
