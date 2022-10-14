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
@Document(collection = "note_sql")
@ApiModel(value = "发布说明数据库文件信息表")
public class NoteSql implements Serializable {

	private static final long serialVersionUID = 8266994712527546981L;

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;
	
	@Field("id")
	private String id;
	
	@Field("file_principal")
	@ApiModelProperty(value = "文件负责人")
	private String file_principal;

	@Field("principal_phone")
	@ApiModelProperty(value = "负责人联系方式")
	private String principal_phone;
	
	@Field("release_node_name")
	@ApiModelProperty(value = "投产窗口名称")
	private String release_node_name;

	@Field("note_id")
	@ApiModelProperty(value = "发布说明id")
	private String note_id;
	
	@Field("filename")
	@ApiModelProperty(value = "上传文件名")
	private String fileName;

	@Field("asset_catalog_name")
	@ApiModelProperty(value = "介质目录")
	private String asset_catalog_name;
	
	@Field("seq_no")
	@ApiModelProperty(value = "执行序号")
	private String seq_no;
	
	@Field("file_url")
	@ApiModelProperty(value = "文件路径")
	private String file_url;
	
	@Field("file_type")
	@ApiModelProperty(value = "文件类型")
	private String file_type;
	
	@Field("catalog_type")
	@ApiModelProperty(value = "目录类型")
	private String catalog_type;// 3-sql文件
	
	@Field("script_type")
	@ApiModelProperty(value = "脚本类型")
	private String script_type;// 1-上传脚本 2-执行脚本 3-回退脚本
	
	@Field("sql_type")
	@ApiModelProperty(value = "sql类型")
	private String sql_type;
	
	@Field("stop_type")
	@ApiModelProperty(value = "停止状态")
	private String stop_type;//1-停止前 2-停止中 3-停止后

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

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getAsset_catalog_name() {
		return asset_catalog_name;
	}

	public void setAsset_catalog_name(String asset_catalog_name) {
		this.asset_catalog_name = asset_catalog_name;
	}

	public String getSeq_no() {
		return seq_no;
	}

	public void setSeq_no(String seq_no) {
		this.seq_no = seq_no;
	}

	public String getFile_url() {
		return file_url;
	}

	public void setFile_url(String file_url) {
		this.file_url = file_url;
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

	public String getSql_type() {
		return sql_type;
	}

	public void setSql_type(String sql_type) {
		this.sql_type = sql_type;
	}

	public String getScript_type() {
		return script_type;
	}

	public void setScript_type(String script_type) {
		this.script_type = script_type;
	}

	public String getStop_type() {
		return stop_type;
	}

	public void setStop_type(String stop_type) {
		this.stop_type = stop_type;
	}
}
