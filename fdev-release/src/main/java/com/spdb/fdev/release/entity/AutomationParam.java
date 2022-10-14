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
@Document(collection = "automation_param")
@ApiModel(value = "自动化发布参数的对应表")
public class AutomationParam implements Serializable {

	private static final long serialVersionUID = 7496633523309556754L;

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;

	@Field("id")
	@Indexed(unique = true)
	private String id;

	@Field("key")
	@Indexed(unique = true)
	@ApiModelProperty(value = "key")
	private String key;

	@Field("value")
	@ApiModelProperty(value = "value")
	private String value;

	@Field("description")
	@ApiModelProperty(value = "描述")
	private String description;

	public AutomationParam() {
		super();
	}

	public AutomationParam(String id, String key, String value, String description) {
		this.id = id;
		this.key = key;
		this.value = value;
		this.description = description;
	}

	public AutomationParam(ObjectId _id, String id, String key, String value, String description) {
		this._id = _id;
		this.id = id;
		this.key = key;
		this.value = value;
		this.description = description;
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

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
