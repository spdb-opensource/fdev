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
@Document(collection = "note_configuration")
@ApiModel(value = "发布说明配置文件信息表")
public class NoteConfiguration implements Serializable {

	private static final long serialVersionUID = 8266994712527544559L;

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;
	
	@Field("id")
	private String id;
	
	@Field("module_name")
	@ApiModelProperty(value = "模块名")
	private String module_name;
	
	@Field("module_ip")
	@ApiModelProperty(value = "模块IP")
	private String module_ip;

	@Field("note_id")
	@ApiModelProperty(value = "发布说明id")
	private String note_id;
	
	@Field("release_node_name")
	@ApiModelProperty(value = "投产窗口名称")
	private String release_node_name;
	
	@Field("fileName")
	@ApiModelProperty(value = "文件名")
	private String fileName;
	
	@Field("file_url")
	@ApiModelProperty(value = "文件路径")
	private String file_url;
	
	@Field("file_principal")
	@ApiModelProperty(value = "文件负责人")
	private String file_principal;

	@Field("principal_phone")
	@ApiModelProperty(value = "负责人联系方式")
	private String principal_phone;
	
	@Field("diff_content")
	@ApiModelProperty(value = "diff结果")
	private List<Map<String, Object>> diff_content;

	@Field("file_type")
	@ApiModelProperty(value = "文件类型")
	private String file_type;
	
	@Field("catalog_type")
	@ApiModelProperty(value = "目录类型")
	private String catalog_type;// 2-配置文件

	@Field("city")
	@ApiModelProperty(value = "对比地区")
	private String city;// 1-上海 2-合肥 3-上海，合肥

	@Field("safeguard_explain")
	@ApiModelProperty(value = "维护说明")
	private String safeguard_explain;
	
	@Field("diff_flag")
	@ApiModelProperty(value = "diff状态")
	private String diff_flag;
	
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

	public String getModule_name() {
		return module_name;
	}

	public void setModule_name(String module_name) {
		this.module_name = module_name;
	}

	public String getModule_ip() {
		return module_ip;
	}

	public void setModule_ip(String module_ip) {
		this.module_ip = module_ip;
	}

	public String getNote_id() {
		return note_id;
	}

	public void setNote_id(String note_id) {
		this.note_id = note_id;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFile_url() {
		return file_url;
	}

	public void setFile_url(String file_url) {
		this.file_url = file_url;
	}

	public String getFile_principal() {
		return file_principal;
	}

	public void setFile_principal(String file_principal) {
		this.file_principal = file_principal;
	}

	public String getPrincipal_phone() {
		return principal_phone;
	}

	public void setPrincipal_phone(String principal_phone) {
		this.principal_phone = principal_phone;
	}

	public List<Map<String, Object>> getDiff_content() {
		return diff_content;
	}

	public void setDiff_content(List<Map<String, Object>> diff_content) {
		this.diff_content = diff_content;
	}

	public String getFile_type() {
		return file_type;
	}

	public void setFile_type(String file_type) {
		this.file_type = file_type;
	}

	public String getCatalog_type() {
		return catalog_type;
	}

	public void setCatalog_type(String catalog_type) {
		this.catalog_type = catalog_type;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getSafeguard_explain() {
		return safeguard_explain;
	}

	public void setSafeguard_explain(String safeguard_explain) {
		this.safeguard_explain = safeguard_explain;
	}

	public String getDiff_flag() {
		return diff_flag;
	}

	public void setDiff_flag(String diff_flag) {
		this.diff_flag = diff_flag;
	}
	
}
