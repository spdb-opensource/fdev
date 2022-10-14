package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * @author xxx
 * @date 2019/7/5 13:15
 */
@Document(collection = "model")
@ApiModel(value = "实体")
public class Model implements Serializable {

	private static final long serialVersionUID = 8637314009320661611L;

	@Id
	@ApiModelProperty(value = "ID")
	@JsonIgnore
	private ObjectId _id;

	@Field("id")
	@ApiModelProperty(value = "实体id")
	private String id;

	@Indexed(unique = true)
	@Field("name_en")
	@ApiModelProperty(value = "实体英文")
	private String name_en;

	@Indexed(unique = true)
	@Field("name_cn")
	@ApiModelProperty(value = "实体中文")
	private String name_cn;

	@Field("first_category")
	@ApiModelProperty(value = "一级分类")
	private String first_category;

	@Field("second_category")
	@ApiModelProperty(value = "二级分类")
	private String second_category;

	@Field("suffix_name")
	@ApiModelProperty(value = "后缀名")
	private String suffix_name;

	@Field("scope")
	@ApiModelProperty(value = "作用域")
	private String scope;

	@Field("version")
	@ApiModelProperty(value = "实体版本")
	private String version;

	@Field("desc")
	@ApiModelProperty(value = "实体描述")
	private String desc;

	@Field("env_key")
	@ApiModelProperty(value = "实体变量key")
	private List<Object> env_key;

	@Field("status")
	@ApiModelProperty(value = "有效状态")
	private String status;

	@Field("ctime")
	@ApiModelProperty(value = "创建时间")
	private String ctime;

	@Field("utime")
	@ApiModelProperty(value = "修改时间")
	private String utime;

	@Field("opno")
	@ApiModelProperty(value = "操作员")
	private String opno;

	@Field(value = "model_template_id")
	@ApiModelProperty(value = "实体模板ID")
	private String model_template_id;

	@Field(value = "platform")
	@ApiModelProperty(value = "适用平台")
	private String platform;

	public Model() {
	}

	public Model(ObjectId _id, String id, String name_en, String name_cn, String first_category, String second_category,
			String suffix_name, String scope, String version, String desc, List<Object> env_key, String status,
			String ctime, String utime, String opno) {
		super();
		this._id = _id;
		this.id = id;
		this.name_en = name_en;
		this.name_cn = name_cn;
		this.first_category = first_category;
		this.second_category = second_category;
		this.suffix_name = suffix_name;
		this.scope = scope;
		this.version = version;
		this.desc = desc;
		this.env_key = env_key;
		this.status = status;
		this.ctime = ctime;
		this.utime = utime;
		this.opno = opno;
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

	public String getName_en() {
		return name_en;
	}

	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	public String getName_cn() {
		return name_cn;
	}

	public void setName_cn(String name_cn) {
		this.name_cn = name_cn;
	}

	public String getFirst_category() {
		return first_category;
	}

	public void setFirst_category(String first_category) {
		this.first_category = first_category;
	}

	public String getSecond_category() {
		return second_category;
	}

	public void setSecond_category(String second_category) {
		this.second_category = second_category;
	}

	public String getSuffix_name() {
		return suffix_name;
	}

	public void setSuffix_name(String suffix_name) {
		this.suffix_name = suffix_name;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<Object> getEnv_key() {
		return env_key;
	}

	public void setEnv_key(List<Object> env_key) {
		this.env_key = env_key;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getCtime() {
		return ctime;
	}

	public void setCtime(String ctime) {
		this.ctime = ctime;
	}

	public String getUtime() {
		return utime;
	}

	public void setUtime(String utime) {
		this.utime = utime;
	}

	public String getOpno() {
		return opno;
	}

	public void setOpno(String opno) {
		this.opno = opno;
	}

	public String getModel_template_id() {
		return model_template_id;
	}

	public void setModel_template_id(String model_template_id) {
		this.model_template_id = model_template_id;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	@Override
	public String toString() {
		return "Model{" + "_id=" + _id + ", id='" + id + '\'' + ", name_en='" + name_en + '\'' + ", name_cn='" + name_cn
				+ '\'' + ", first_category='" + first_category + '\'' + ", second_category='" + second_category + '\''
				+ ", suffix_name='" + suffix_name + '\'' + ", scope='" + scope + '\'' + ", version='" + version + '\''
				+ ", desc='" + desc + '\'' + ", env_key=" + env_key + ", status='" + status + '\'' + ", ctime='" + ctime
				+ '\'' + ", utime='" + utime + '\'' + ", opno='" + opno + '\'' + '}';
	}
}
