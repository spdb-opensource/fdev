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
import java.util.Map;

@Document(collection = "prod_application")
@ApiModel(value = "变更应用表")
@CompoundIndexes({
		@CompoundIndex(name = "prod_app_idx", def = "{'prod_id': 1, 'application_id': 1}", unique = true)
})
public class ProdApplication implements Serializable{

	/**
	 * 变更应用表
	 */
	private static final long serialVersionUID = 7316536748843465970L;
	@Id
	private ObjectId _id;
	
	@Field("prod_id")
	@ApiModelProperty(value = "变更id")
	private String prod_id;
	
	@Field("template_id")
	@ApiModelProperty(value = "变更模板id")
	private String template_id;
	
	@Field("application_id")
	@ApiModelProperty(value = "应用编号")
	private String application_id;
	
	@Field("pro_image_uri")
	@ApiModelProperty(value = "投产镜像标签")
	private String pro_image_uri;

	@Field("pro_scc_image_uri")
	@ApiModelProperty(value = "scc投产镜像标签")
	private String pro_scc_image_uri;

	@Field("status")
	@ApiModelProperty(value = "变更应用介质准备状态")
	private String status;

	@Field("fdev_config_changed")
	@ApiModelProperty(value = "最新已投产版本配置文件与当前选择版本配置文件是否存在差异")
	private boolean fdev_config_changed;

	@Field("compare_url")
	@ApiModelProperty(value = "gitlab分支对比url")
	private String compare_url;

	@Field("release_type")
	@ApiModelProperty(value = "应用类型")
	private String release_type;

	@Field("container_num")
	@ApiModelProperty(value = "容器数量")
	private String container_num;

	@Field("fdev_config_confirm")
	@ApiModelProperty(value = "配置文件审核状态")
	private String fdev_config_confirm;

	@Field("fake_image_uri")
	@ApiModelProperty(value = "faketime分支镜像标签")
	private String fake_image_uri;

	@Field("create_time")
	@ApiModelProperty(value="创建时间")
	private String create_time;

	@Field("update_time")
	@ApiModelProperty(value="修改时间")
	private String update_time;

	@Field("caas_add_sign")
	@ApiModelProperty(value="新增caas变更标识 1：新增，0：非新增")
	private String caas_add_sign;

	@Field("scc_add_sign")
	@ApiModelProperty(value="新增scc变更标识 1：新增，0：非新增")
	private String scc_add_sign;

	@Field("tag")
	@ApiModelProperty(value = "配置文件变更所使用配置文件tag")
	private String tag;

	@Field("pro_package_uri")
	@ApiModelProperty(value="变更包地址")
	private String pro_package_uri;

	@Field("prod_dir")
	@ApiModelProperty(value="变更目录")
	private List<String> prod_dir;

	@Field("change")
	@ApiModelProperty(value="change")
	private Map<String,Object> change;
	
	@Field("deploy_type")
	@ApiModelProperty(value="部署平台")
	private List<String> deploy_type;

	@Field("esf_flag")
	@ApiModelProperty(value="是否有esf的应用记录")
	private String esf_flag;

	@Field("esf_platform")
	@ApiModelProperty(value="esf的应用部署平台")
	private List<String> esf_platform;

	@Field("caas_stop_env")
	@ApiModelProperty(value="docker_stop目录需要停止的集群")
	private List<String> caas_stop_env;

	@Field("scc_stop_env")
	@ApiModelProperty(value="scc_stop目录需要停止的集群")
	private List<String> scc_stop_env;

	public ProdApplication() {
		super();
	}

	public ProdApplication(String prod_id) {
		this.prod_id = prod_id;
	}

	public ProdApplication(String prod_id, String release_type) {
		this.prod_id = prod_id;
		this.release_type = release_type;
	}

	public ProdApplication(String prod_id, String application_id, String pro_image_uri,String pro_scc_image_uri) {
		this.prod_id = prod_id;
		this.application_id = application_id;
		this.pro_image_uri = pro_image_uri;
		this.pro_scc_image_uri = pro_scc_image_uri;
	}

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getProd_id() {
		return prod_id;
	}

	public void setProd_id(String prod_id) {
		this.prod_id = prod_id;
	}

	public String getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(String template_id) {
		this.template_id = template_id;
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public boolean isFdev_config_changed() {
		return fdev_config_changed;
	}

	public void setFdev_config_changed(boolean fdev_config_changed) {
		this.fdev_config_changed = fdev_config_changed;
	}

	public String getCompare_url() {
		return compare_url;
	}

	public void setCompare_url(String compare_url) {
		this.compare_url = compare_url;
	}

	public String getRelease_type() {
		return release_type;
	}

	public void setRelease_type(String release_type) {
		this.release_type = release_type;
	}

	public String getContainer_num() {
		return container_num;
	}

	public void setContainer_num(String container_num) {
		this.container_num = container_num;
	}

	public String getFdev_config_confirm() {
		return fdev_config_confirm;
	}

	public void setFdev_config_confirm(String fdev_config_confirm) {
		this.fdev_config_confirm = fdev_config_confirm;
	}

	public String getFake_image_uri() {
		return fake_image_uri;
	}

	public void setFake_image_uri(String fake_image_uri) {
		this.fake_image_uri = fake_image_uri;
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

	public String getCaas_add_sign() {
		return caas_add_sign;
	}

	public void setCaas_add_sign(String caas_add_sign) {
		this.caas_add_sign = caas_add_sign;
	}

	public String getScc_add_sign() {
		return scc_add_sign;
	}

	public void setScc_add_sign(String scc_add_sign) {
		this.scc_add_sign = scc_add_sign;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getPro_package_uri() { return pro_package_uri; }

	public void setPro_package_uri(String pro_package_uri) { this.pro_package_uri = pro_package_uri; }

	public List<String> getProd_dir() {
		return prod_dir;
	}

	public void setProd_dir(List<String> prod_dir) {
		this.prod_dir = prod_dir;
	}

	public Map<String, Object> getChange() {
		return change;
	}

	public void setChange(Map<String, Object> change) {
		this.change = change;
	}

	public List<String> getDeploy_type() {
		return deploy_type;
	}

	public void setDeploy_type(List<String> deploy_type) {
		this.deploy_type = deploy_type;
	}

	public String getEsf_flag() {
		return esf_flag;
	}

	public void setEsf_flag(String esf_flag) {
		this.esf_flag = esf_flag;
	}

	public List<String> getEsf_platform() {
		return esf_platform;
	}

	public void setEsf_platform(List<String> esf_platform) {
		this.esf_platform = esf_platform;
	}

	public String getPro_scc_image_uri() {
		return pro_scc_image_uri;
	}

	public void setPro_scc_image_uri(String pro_scc_image_uri) {
		this.pro_scc_image_uri = pro_scc_image_uri;
	}

	public List<String> getCaas_stop_env() {
		return caas_stop_env;
	}

	public void setCaas_stop_env(List<String> caas_stop_env) {
		this.caas_stop_env = caas_stop_env;
	}

	public List<String> getScc_stop_env() {
		return scc_stop_env;
	}

	public void setScc_stop_env(List<String> scc_stop_env) {
		this.scc_stop_env = scc_stop_env;
	}
}
