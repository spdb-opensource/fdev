package com.spdb.fdev.release.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Component
@Document(collection = "note_manual")
@ApiModel(value = "手动发布说明信息表")
public class NoteManual implements Serializable {

	private static final long serialVersionUID = 8266994712527548867L;

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;
	
	@Field("id")
	private String id;
	
	@Field("note_info")
	@ApiModelProperty(value = "手动发布说明信息")
	private String note_info;
	
	@Field("note_id")
	@ApiModelProperty(value = "手动发布说明Id")
	private String note_id;

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

	public String getNote_info() {
		return note_info;
	}

	public void setNote_info(String note_info) {
		this.note_info = note_info;
	}

	public String getNote_id() {
		return note_id;
	}

	public void setNote_id(String note_id) {
		this.note_id = note_id;
	}
}
