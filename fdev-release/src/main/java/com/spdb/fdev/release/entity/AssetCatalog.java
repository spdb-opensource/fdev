package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "asset_catalog")
@ApiModel(value = "介质目录表")
@CompoundIndexes({
		@CompoundIndex(name = "asset_catalog_namex", def = "{'template_id': 1, 'catalog_name': 1}", unique = true)
})
public class AssetCatalog {

	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;

	@Field("id")
	@Indexed(unique = true)
	private String id;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Field("template_id")
	@ApiModelProperty(value = "变更模板id")
	private String template_id;

	@Field("catalog_name")
	@ApiModelProperty(value = "目录名")
	private String catalog_name;

	@Field("description")
	@ApiModelProperty(value = "描述")
	private String description;

	@Field("catalog_type")
	@ApiModelProperty(value = "目录类型")
	private String catalog_type;

	@Field("create_time")
	@ApiModelProperty(value="创建时间")
	private String create_time;

	@Field("update_time")
	@ApiModelProperty(value="修改时间")
	private String update_time;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(String template_id) {
		this.template_id = template_id;
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

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

}
