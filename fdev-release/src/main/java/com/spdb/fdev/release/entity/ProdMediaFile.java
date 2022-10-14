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
import java.util.List;

@Component
@Document(collection = "prod_media_file")
@ApiModel(value = "变更介质目录表")
public class ProdMediaFile implements Serializable {

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;

	@Field("id")
	@Indexed(unique = true)
	private String id;

	@Field("prod_id")
	@ApiModelProperty(value = "变更编号")
	private String prod_id;

	@Field("directory")
	@ApiModelProperty(value = "介质大目录")
	private String directory;

	@Field("directory_details")
	@ApiModelProperty(value = "介质大目录详情")
	private List<String> directory_details;

	public ProdMediaFile() {
		super();
	}

	public ProdMediaFile(ObjectId _id, String id, String prod_id, String directory, List<String> directory_details) {
		this._id = _id;
		this.id = id;
		this.prod_id = prod_id;
		this.directory = directory;
		this.directory_details = directory_details;
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

	public String getProd_id() {
		return prod_id;
	}

	public void setProd_id(String prod_id) {
		this.prod_id = prod_id;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public List<String> getDirectory_details() {
		return directory_details;
	}

	public void setDirectory_details(List<String> directory_details) {
		this.directory_details = directory_details;
	}
}
