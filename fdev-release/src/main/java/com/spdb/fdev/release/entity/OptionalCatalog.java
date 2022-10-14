package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "optional_catalog")
@ApiModel(value = "介质可选目录表")
public class OptionalCatalog {

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;

	@Field("id")
	@Indexed(unique = true)
	private String id;
	
	@Field("catalog_name")
	@Indexed(unique = true)
	@ApiModelProperty(value = "目录名")
	private String catalog_name;
	
	@Field("description")
	@ApiModelProperty(value = "描述")
	private String description;
	  
	@Field("catalog_type")
	@ApiModelProperty(value = "目录类型")
	private String catalog_type;

	public OptionalCatalog() {
		super();
	}

    public OptionalCatalog(String id, String catalog_name, String catalog_type, String description) {
        this.id = id;
        this.catalog_name = catalog_name;
        this.catalog_type = catalog_type;
        this.description = description;
    }

	public OptionalCatalog(ObjectId _id, String id, String catalog_name, String catalog_type, String description) {
		this._id = _id;
		this.id = id;
		this.catalog_name = catalog_name;
		this.catalog_type = catalog_type;
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

	public String getCatalog_name() {
		return catalog_name;
	}

	public void setCatalog_name(String catalog_name) {
		this.catalog_name = catalog_name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCatalog_type() {
		return catalog_type;
	}

	public void setCatalog_type(String catalog_type) {
		this.catalog_type = catalog_type;
	}
}
