package com.spdb.fdev.release.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Component
@Document(collection = "note_service")
@ApiModel(value = "发布说明应用信息表")
public class NoteService implements Serializable {

	private static final long serialVersionUID = 8266994712527577995L;

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;
	
	@Field("id")
	private String id;
	
	@Field("application_name_en")
	@ApiModelProperty(value = "应用英文名称")
	private String application_name_en;
	
	@Field("application_name_cn")
	@ApiModelProperty(value = "应用中文名称")
	private String application_name_cn;
	
	@Field("release_node_name")
	@ApiModelProperty(value = "投产窗口名称")
	private String release_node_name;

	@Field("note_id")
	@ApiModelProperty(value = "发布说明id")
	private String note_id;
	
	@Field("dev_managers_info")
	@ApiModelProperty(value = "应用负责人信息")
	private List<Map<String,Object>> dev_managers_info;
	
	@Field("tag_name")
	@ApiModelProperty(value = "投产tag名称")
	private String tag_name;
	
	@Field("application_id")
	@ApiModelProperty(value = "应用id")
	private String application_id;
	
	@Field("catalog_type")
	@ApiModelProperty(value = "目录类型")
	private String catalog_type;// 1-应用信息

	@Field("application_type")
	@ApiModelProperty(value = "应用类型")
	private String application_type;
	
	@Field("expand_info")
	@ApiModelProperty(value = "扩展信息")
	private Map<String, Object> expand_info;
	
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

	public String getApplication_name_en() {
		return application_name_en;
	}

	public void setApplication_name_en(String application_name_en) {
		this.application_name_en = application_name_en;
	}

	public String getApplication_name_cn() {
		return application_name_cn;
	}

	public void setApplication_name_cn(String application_name_cn) {
		this.application_name_cn = application_name_cn;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
	}

	public String getNote_id() {
		return note_id;
	}

	public void setNote_id(String note_id) {
		this.note_id = note_id;
	}

	public List<Map<String, Object>> getDev_managers_info() {
		return dev_managers_info;
	}

	public void setDev_managers_info(List<Map<String, Object>> dev_managers_info) {
		this.dev_managers_info = dev_managers_info;
	}

	public String getTag_name() {
		return tag_name;
	}

	public void setTag_name(String tag_name) {
		this.tag_name = tag_name;
	}

	public String getApplication_id() {
		return application_id;
	}

	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}

	public String getCatalog_type() {
		return catalog_type;
	}

	public void setCatalog_type(String catalog_type) {
		this.catalog_type = catalog_type;
	}

	public String getApplication_type() {
		return application_type;
	}

	public void setApplication_type(String application_type) {
		this.application_type = application_type;
	}

	public Map<String, Object> getExpand_info() {
		return expand_info;
	}

	public void setExpand_info(Map<String, Object> expand_info) {
		this.expand_info = expand_info;
	}

}
