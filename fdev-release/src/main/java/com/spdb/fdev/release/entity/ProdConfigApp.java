package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = "prod_config_app")
@ApiModel(value = "配置文件变更应用表")
@CompoundIndexes({
		@CompoundIndex(name = "prod_app_idx", def = "{'release_node_name': 1, 'application_id': 1}", unique = true)
})
public class ProdConfigApp implements Serializable{

	/**
	 * 配置文件变更应用表
	 */
	private static final long serialVersionUID = 7316536748843465970L;
	@Id
	private ObjectId _id;

	@Field("application_id")
	@ApiModelProperty(value = "应用编号")
	private String application_id;

	@Field("application_name")
	@ApiModelProperty(value = "应用名称")
	private String application_name;

	@Field("dev_managers")
	@ApiModelProperty(value = "应用负责人")
	private List<String> dev_managers;

	@Field("config_type")
	@ApiModelProperty(value = "实体属性")
	private List<String> config_type;

	@Field("release_node_name")
	@ApiModelProperty(value = "投产点名称")
	private String release_node_name;

	@Field("release_node")
	@ApiModelProperty(value = "挂载投产窗口")
	private String release_node;

	@Field("status")
	@ApiModelProperty(value = "配置文件生成状态（0：未生成，1：已生成）")
	private String status;

	@Field("isrisk")
	@ApiModelProperty(value = "是否风险（0：否，1：是）")
	private String isrisk;

	@Field("ischeck")
	@ApiModelProperty(value = "是否审核（0：否，1：是）")
	private String ischeck;

	@Field("asset_name")
	@ApiModelProperty(value = "介质名称")
	private String asset_name;

	@Field("pro_image_uri")
	@ApiModelProperty(value = "投产镜像标签")
	private String pro_image_uri;

	@Field("tag")
	@ApiModelProperty(value = "配置文件tag")
	private String tag;

	@Field("date")
	@ApiModelProperty(value = "创建更新时间")
	private String date;


	public ProdConfigApp() {
		super();
	}

	public ProdConfigApp(ObjectId _id, String application_id, String application_name, List<String> dev_managers, List<String> config_type, String release_node_name, String release_node, String status, String isrisk, String ischeck, String asset_name, String pro_image_uri, String tag, String date) {
		this._id = _id;
		this.application_id = application_id;
		this.application_name = application_name;
		this.dev_managers = dev_managers;
		this.config_type = config_type;
		this.release_node_name = release_node_name;
		this.release_node = release_node;
		this.status = status;
		this.isrisk = isrisk;
		this.ischeck = ischeck;
		this.asset_name = asset_name;
		this.pro_image_uri = pro_image_uri;
		this.tag = tag;
		this.date = date;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getApplication_id() {
		return application_id;
	}

	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}

	public String getApplication_name() {
		return application_name;
	}

	public void setApplication_name(String application_name) {
		this.application_name = application_name;
	}

	public List<String> getDev_managers() {
		return dev_managers;
	}

	public void setDev_managers(List<String> dev_managers) {
		this.dev_managers = dev_managers;
	}

	public List<String> getConfig_type() {
		return config_type;
	}

	public void setConfig_type(List<String> config_type) {
		this.config_type = config_type;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
	}

	public String getRelease_node() {
		return release_node;
	}

	public void setRelease_node(String release_node) {
		this.release_node = release_node;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIsrisk() {
		return isrisk;
	}

	public void setIsrisk(String isrisk) {
		this.isrisk = isrisk;
	}

	public String getIscheck() {
		return ischeck;
	}

	public void setIscheck(String ischeck) {
		this.ischeck = ischeck;
	}

	public String getAsset_name() {
		return asset_name;
	}

	public void setAsset_name(String asset_name) {
		this.asset_name = asset_name;
	}

	public String getPro_image_uri() {
		return pro_image_uri;
	}

	public void setPro_image_uri(String pro_image_uri) {
		this.pro_image_uri = pro_image_uri;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
