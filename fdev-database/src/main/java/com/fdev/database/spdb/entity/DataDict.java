package com.fdev.database.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "dataDict")
@ApiModel(value="数据字典表")
public class DataDict implements Serializable {
	private static final long serialVersionUID = 7528659211794720516L;
	@Id
	private ObjectId _id;

	@Field("dict_id")
	@ApiModelProperty(value="字典id")
	private String dict_id;

	@Field("database_type")
	@ApiModelProperty(value="数据库类型")
	private String database_type;

	@Field("database_name")
	@ApiModelProperty(value="库名")
	private String database_name;

	@Field("field_en_name")
	@ApiModelProperty(value="字段英文名")
	private String field_en_name;
	
	@Field("field_ch_name")
	@ApiModelProperty(value="字段中文名")
	private String field_ch_name;

	@Field("field_type")
	@ApiModelProperty(value="字段类型")
	private String field_type;

	@Field("field_length")
	@ApiModelProperty(value="字段长度")
	private String field_length;

	@Field("status")
	@ApiModelProperty(value="字段使用状态, 0是废弃, 1是使用")
	private String status;

	public String getDict_id() {
		return dict_id;
	}

	public void setDict_id(String dict_id) {
		this.dict_id = dict_id;
	}

	public String getDatabase_type() {
		return database_type;
	}

	public void setDatabase_type(String database_type) {
		this.database_type = database_type;
	}

	public String getDatabase_name() {
		return database_name;
	}

	public void setDatabase_name(String database_name) {
		this.database_name = database_name;
	}

	public String getField_en_name() {
		return field_en_name;
	}

	public void setField_en_name(String field_en_name) {
		this.field_en_name = field_en_name;
	}

	public String getField_ch_name() {
		return field_ch_name;
	}

	public void setField_ch_name(String field_ch_name) {
		this.field_ch_name = field_ch_name;
	}

	public String getField_type() {
		return field_type;
	}

	public void setField_type(String field_type) {
		this.field_type = field_type;
	}

	public String getField_length() {
		return field_length;
	}

	public void setField_length(String field_length) {
		this.field_length = field_length;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public DataDict() {

	}

	public DataDict(String dict_id, String database_type, String database_name, String field_en_name, String field_ch_name, String field_type, String field_length, String status) {
		this.dict_id = dict_id;
		this.database_type = database_type;
		this.database_name = database_name;
		this.field_en_name = field_en_name;
		this.field_ch_name = field_ch_name;
		this.field_type = field_type;
		this.field_length = field_length;
		this.status = status;
	}

	@Override
	public String toString() {
		return "DataDict{" +
				"dict_id='" + dict_id + '\'' +
				", database_type='" + database_type + '\'' +
				", database_name='" + database_name + '\'' +
				", field_en_name='" + field_en_name + '\'' +
				", field_ch_name='" + field_ch_name + '\'' +
				", field_type='" + field_type + '\'' +
				", field_length='" + field_length + '\'' +
				", status='" + status + '\'' +
				'}';
	}

	public void initId() {
		ObjectId temp = new ObjectId();
		this.dict_id=temp.toString();
		this._id=temp;
	}
}
