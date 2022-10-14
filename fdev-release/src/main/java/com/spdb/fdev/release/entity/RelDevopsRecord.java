package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "rel_devops_record")
@ApiModel(value = "持续集成记录表")

public class RelDevopsRecord implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7006569285795826295L;
	@Id
	private ObjectId _id;

	@Field("release_node_name")
	@ApiModelProperty(value = "投产窗口名称")
	private String release_node_name;
	
	@Field("id")
	private String id;
	
	@Field("product_tag")
	@ApiModelProperty(value = "投产tag分支名")
	private String product_tag;
	

	@Field("application_id")
	@ApiModelProperty(value = "应用id")
	private String application_id;
	
	@Field("pro_image_uri")
	@ApiModelProperty(value = "投产镜像标签")
	private String pro_image_uri;
	
	@Field("merge_request_id")
	@ApiModelProperty(value = "mergeid")
	private String merge_request_id;
	
	@Field("devops_status")
	@ApiModelProperty(value = "持续集成状态")
	private String devops_status;
	
	@Field("devops_type")
	@ApiModelProperty(value = "持续集成类型")
	private String devops_type;

	@Field("tag_version")
	@ApiModelProperty(value = "客户端版本号")
	private String tag_version;

	@Field("is_reinforce")
	@ApiModelProperty(value = "是否加固")
	private String is_reinforce;

	@Field("create_time")
	@ApiModelProperty(value="创建时间")
	private String create_time;

	@Field("update_time")
	@ApiModelProperty(value="修改时间")
	private String update_time;

	@Field("pro_package_uri")
	@ApiModelProperty(value="变更包地址")
	private String pro_package_uri;

	@Field("pro_scc_image_uri")
	@ApiModelProperty(value = "scc投产镜像标签")
	private String pro_scc_image_uri;

	@Field("caas_env")
	@ApiModelProperty(value = "caas的环境")
	private String caas_env;

	@Field("scc_env")
	@ApiModelProperty(value = "scc的环境")
	private String scc_env;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public String getIs_reinforce() {
		return is_reinforce;
	}

	public void setIs_reinforce(String is_reinforce) {
		this.is_reinforce = is_reinforce;
	}

	public String getProduct_tag() {
		return product_tag;
	}

	public void setProduct_tag(String product_tag) {
		this.product_tag = product_tag;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
	}

	public String getApplication_id() {
		return application_id;
	}

	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}

	public String getPro_image_uri() {
		return pro_image_uri;
	}

	public void setPro_image_uri(String pro_image_uri) {
		this.pro_image_uri = pro_image_uri;
	}

	public String getMerge_request_id() {
		return merge_request_id;
	}

	public void setMerge_request_id(String merge_request_id) {
		this.merge_request_id = merge_request_id;
	}

	public String getDevops_status() {
		return devops_status;
	}

	public void setDevops_status(String devops_status) {
		this.devops_status = devops_status;
	}

	public String getDevops_type() {
		return devops_type;
	}

	public void setDevops_type(String devops_type) {
		this.devops_type = devops_type;
	}

	public String getTag_version() {
		return tag_version;
	}

	public void setTag_version(String tag_version) {
		this.tag_version = tag_version;
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

	public String getPro_package_uri() { return pro_package_uri; }

	public void setPro_package_uri(String pro_package_uri) { this.pro_package_uri = pro_package_uri; }

	public String getPro_scc_image_uri() {
		return this.pro_scc_image_uri;
	}

	public void setPro_scc_image_uri(String pro_scc_image_uri) {
		this.pro_scc_image_uri = pro_scc_image_uri;
	}

	public String getCaas_env() {
		return caas_env;
	}

	public void setCaas_env(String caas_env) {
		this.caas_env = caas_env;
	}

	public String getScc_env() {
		return scc_env;
	}

	public void setScc_env(String scc_env) {
		this.scc_env = scc_env;
	}
}