package com.spdb.fdev.fdevapp.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "domains")
@ApiModel(value="服务域")
public class DomainEntity implements Serializable {
	
	private static final long serialVersionUID = -5627804709275451840L;
	
	@Id
	private ObjectId _id;
	
	@Field("id")
	@ApiModelProperty(value="服务域id")
	private String id;
	
	@Field("name_en")
	@ApiModelProperty(value="服务域英文缩写")
	private String name_en;
	
	@Field("name_cn")
	@ApiModelProperty(value="服务域中文名")
	private String name_cn;

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

	public String getName_en() {
		return name_en;
	}

	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	public String getName_cn() {
		return name_cn;
	}

	public void setName_cn(String name_cn) {
		this.name_cn = name_cn;
	}

}
