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

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Component
@Document(collection = "prod_record")
@ApiModel(value = "变更记录表")
@CompoundIndexes({
		@CompoundIndex(name = "prod_record_idx", def = "{'release_node_name': 1, 'version': 1}" , unique = true)
})
public class ProdRecord implements Serializable {

	/**
	 * 变更记录实体
	 */
	private static final long serialVersionUID = 3532629789338312731L;
	@Id
	@ApiModelProperty(value = "ID")
	private ObjectId _id;

	@Field("prod_id")
	@NotEmpty(message = "变更记录id不能为空")
	@Indexed(unique = true)
	private String prod_id;

	@Field("release_node_name")
	@NotEmpty(message = "投产窗口名称不能为空")
	private String release_node_name;

	@Field("date")
	@NotEmpty(message = "投产日期不能为空")
	private String date;

	@Field("prod_spdb_no")
	@NotEmpty(message = "变更编号不能为空")
	private String prod_spdb_no;

	@Field("version")
	@NotEmpty(message = "变更版本不能为空")
	private String version;

	@Field("type")
	@NotEmpty(message = "变更类型不能为空")
	private String type;// gray（灰度）、proc（生产）

	@Field("launcher")
	@NotEmpty(message = "变更发起人")
	private String launcher;

	@Field("launcher_name_cn")
	@NotEmpty(message = "变更发起人中文名")
	private String launcher_name_cn;

	@Field("status")
	@NotEmpty(message = "变更状态")
	private String status;// 0-待审核，1-已发布，2-已拒绝，3-成功,4-失败

	@Field("reject_reason")
	@NotEmpty(message = "拒绝理由不能为空")
	private String reject_reason;

	@Field("create_time")
	@NotEmpty(message = "变更创建时间不能为空")
	private String create_time;

	@Field("template_id")
	@NotEmpty(message = "变更模板id不能为空")
	private String template_id;

	@Field("temp_name")
	private String temp_name;

	@Field("owner_system")
	private String owner_system;

	@Field("owner_system_name")
	private String owner_system_name;
	
	@Field("owner_groupId")
	@NotEmpty(message = "小组id不能为空")
	private String owner_groupId;
	
	@Field("assets_giturl")
	private String assets_giturl;
	
	@Field("auto_release_stage")
	private String auto_release_stage;

	@Field("auto_release_log")
	private String auto_release_log;
	
	@Field("template_properties")
	private String template_properties;
	
	@Field("prod_env")
	private String prod_env;
	
	@Field("prod_assets_version")
	private String prod_assets_version;
	
	@Field("plan_time")
	private String plan_time;

	@Field("image_deliver_type")
	private String image_deliver_type;//是否自动化发布（1是0否）

	@Field("excel_template_url")
	private String excel_template_url;

	@Field("excel_template_name")
	private String excel_template_name;
	
	@Field("scc_prod")
	private String scc_prod; // 是否是scc变更（1是0否）

	@Field("update_time")
	@ApiModelProperty(value="修改时间")
	private String update_time;

	@Field("aws_group")
	private String aws_group;

	public String getScc_prod() {
		return scc_prod;
	}

	public void setScc_prod(String scc_prod) {
		this.scc_prod = scc_prod;
	}

	public String getAssets_giturl() {
		return assets_giturl;
	}

	public void setAssets_giturl(String assets_giturl) {
		this.assets_giturl = assets_giturl;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getProd_spdb_no() {
		return prod_spdb_no;
	}

	public void setProd_spdb_no(String prod_spdb_no) {
		this.prod_spdb_no = prod_spdb_no;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLauncher() {
		return launcher;
	}

	public void setLauncher(String launcher) {
		this.launcher = launcher;
	}

	public String getLauncher_name_cn() {
		return launcher_name_cn;
	}

	public void setLauncher_name_cn(String launcher_name_cn) {
		this.launcher_name_cn = launcher_name_cn;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReject_reason() {
		return reject_reason;
	}

	public void setReject_reason(String reject_reason) {
		this.reject_reason = reject_reason;
	}

	public String getCreate_time() {
		return create_time;
	}

	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	public String getTemplate_id() {
		return template_id;
	}

	public void setTemplate_id(String template_id) {
		this.template_id = template_id;
	}

	public String getTemp_name() {
		return temp_name;
	}

	public void setTemp_name(String temp_name) {
		this.temp_name = temp_name;
	}

	public String getOwner_system() {
		return owner_system;
	}

	public void setOwner_system(String owner_system) {
		this.owner_system = owner_system;
	}

	public String getOwner_system_name() {
		return owner_system_name;
	}

	public void setOwner_system_name(String owner_system_name) {
		this.owner_system_name = owner_system_name;
	}

	public String getOwner_groupId() {
		return owner_groupId;
	}

	public void setOwner_groupId(String owner_groupId) {
		this.owner_groupId = owner_groupId;
	}

	public String getAuto_release_stage() {
		return auto_release_stage;
	}

	public void setAuto_release_stage(String auto_release_stage) {
		this.auto_release_stage = auto_release_stage;
	}

	public String getAuto_release_log() {
		return auto_release_log;
	}

	public void setAuto_release_log(String auto_release_log) {
		this.auto_release_log = auto_release_log;
	}

	public String getTemplate_properties() {
		return template_properties;
	}


	public void setTemplate_properties(String template_properties) {
		this.template_properties = template_properties;
	}


	public String getProd_env() {
		return prod_env;
	}


	public void setProd_env(String prod_env) {
		this.prod_env = prod_env;
	}


	public String getProd_assets_version() {
		return prod_assets_version;
	}


	public void setProd_assets_version(String prod_assets_version) {
		this.prod_assets_version = prod_assets_version;
	}


	public String getPlan_time() {
		return plan_time;
	}


	public void setPlan_time(String plan_time) {
		this.plan_time = plan_time;
	}

	public String getImage_deliver_type() {
		return image_deliver_type;
	}

	public void setImage_deliver_type(String image_deliver_type) {
		this.image_deliver_type = image_deliver_type;
	}

	public String getExcel_template_url() {
		return excel_template_url;
	}

	public void setExcel_template_url(String excel_template_url) {
		this.excel_template_url = excel_template_url;
	}

	public String getExcel_template_name() {
		return excel_template_name;
	}

	public void setExcel_template_name(String excel_template_name) {
		this.excel_template_name = excel_template_name;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}

	public String getAws_group() {
		return aws_group;
	}

	public void setAws_group(String aws_group) {
		this.aws_group = aws_group;
	}
}
