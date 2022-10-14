package com.spdb.fdev.fuser.spdb.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = "user")
@ApiModel(value = "用户")
public class User implements Serializable {

	private static final long serialVersionUID = -5382360368973552125L;

	@Id
	@JsonIgnore
	private ObjectId _id;

	@Field("id")
	@ApiModelProperty(value = "用户ID")
	private String id;

	@Field("user_name_en")
	@ApiModelProperty(value = "用户英文名称")
	private String user_name_en;// 用户英文名称

	@Field("user_name_cn")
	@ApiModelProperty(value = "用户中文名称")
	private String user_name_cn;// 用户中文名称

	@Field("email")
	@ApiModelProperty(value = "用户邮箱")
	private String email;// 用户邮箱

	@Field("password")
	@ApiModelProperty(value = "用户密码")
	private String password;// 用户密码 初始化默认为xxx

	@Field("group_id")
	@ApiModelProperty(value = "所属小组ID")
	private String group_id;// group{ id:"id" , groupName : "name" }

	@Field("role_id")
	@ApiModelProperty(value = "角色ID")
	private List<String> role_id;// 角色

	@Field("svn_user")
	@ApiModelProperty(value = "svn帐号")
	private String svn_user;// svn

	@Field("redmine_user")
	@ApiModelProperty(value = "redmine帐号")
	private String redmine_user;// redmine

	@Field("git_user_id")
	@ApiModelProperty(value = "gitlabel用户id")
	private String git_user_id;// git用户id

	@Field("git_user")
	@ApiModelProperty(value = "gitlabel帐号")
	private String git_user;// gitlab用户

	@Field("git_token")
	@ApiModelProperty(value = "git_token")
	private String git_token;// gitlabToken

	@Field("telephone")
	@ApiModelProperty(value = "手机号")
	private String telephone;// 手机号

	@Field("kf_approval")
	@ApiModelProperty(value = "KF白名单开通")
	private Boolean kf_approval;

	@Field("net_move")
	@ApiModelProperty(value = "虚机网段迁移")
	private Boolean net_move;

	@Field("is_spdb")
	@ApiModelProperty(value = "是否为行内人员")
	private Boolean is_spdb;

	@Field("vm_ip")
	@ApiModelProperty(value = "虚机ip地址")
	private String vm_ip;

	@Field("vm_name")
	@ApiModelProperty(value = "虚机名")
	private String vm_name;

	@Field("vm_user_name")
	@ApiModelProperty(value = "虚机用户名/域用户")
	private String vm_user_name;

	@Field("phone_type")
	@ApiModelProperty(value = "手机型号")
	private String phone_type;

	@Field("phone_mac")
	@ApiModelProperty(value = "手机mac地址")
	private String phone_mac;

	@Transient
	@ApiModelProperty(value = "是否提交kf网络开通")
	private Boolean is_kfApproval;

	@Transient
	@ApiModelProperty(value = "是否提交虚机迁移开通")
	private Boolean is_vmApproval;

	@Field("is_spdb_mac")
	@ApiModelProperty(value = "是否为行内测试机的mac地址")
	private String is_spdb_mac; // kf不开通为"" ，"1"标识为行内测试机 , "0"标识为 不是行内测试机

	@Field("company_id")
	@ApiModelProperty(value = "公司ID")
	private String company_id;// 公司

	@Field("status")
	@ApiModelProperty(value = "是否在职，0在职，1离职")
	private String status;// 状态 在职0 离职1

	@Field("is_once_login")
	@ApiModelProperty(value = "是否首次登录，0是, 1不是, 2是新手指引页面, 3是密码重置后")
	private String is_once_login;// 是否首次登录 0 是

	@Field("labels")
	@ApiModelProperty(value = "用户标签ID")
	private List<String> labels;// 用户标签

	@Field("create_date")
	@ApiModelProperty(value = "用户创建时间")
	private String create_date;// 用户创建时间

	@Field("leave_date")
	@ApiModelProperty(value = "用户离职时间")
	private String leave_date;// 用户离职时间

	@Field("area_id")
	@ApiModelProperty(value = "所属地区ID")
	private String area_id;

	@Field("function_id")
	@ApiModelProperty(value = "人员职能ID")
	private String function_id;

	@Field("rank_id")
	@ApiModelProperty(value = "职级ID")
	private String rank_id;

	@Field("education")
	@ApiModelProperty(value = "学历")
	private String education;// 学历

	@Field("start_time")
	@ApiModelProperty(value = "工作开始时间")
	private String start_time;// 工作开始时间

	@Field("remark")
	@ApiModelProperty(value = "备注")
	private String remark;// 备注

	@Field("ftms_level")
	@ApiModelProperty(value = "用户测试等级,1为初级，2为中级，3为高级")
	private String ftms_level;// 用户测试等级

	@Field("mantis_token")
	@ApiModelProperty(value = "mantis密钥")
	private String mantis_token;// mantis密钥

	@Field("createTime")
	@ApiModelProperty(value = "数据创建时间")
	private String createTime;// 数据创建时间

	@Field("updateTime")
	@ApiModelProperty(value = "数据最后修改时间")
	private String updateTime;// 数据最后修改时间

	@Field("is_party_member")
	@ApiModelProperty(value = "是否党员")
	private String is_party_member;// 是否党员，0是，1不是

	@Field("section")
	@ApiModelProperty(value = "所属条线")
	private String section;

	@Field("work_num")
	@ApiModelProperty(value = "工号")
	private String work_num;

	@Field("is_manage_group")
	@ApiModelProperty(value = "是否小组负责人")
	private String is_manage_group;// 0是，1不是

	@Field("deploy_white")
	@ApiModelProperty(value = "部署白名单用户")
	private String deploy_white;// 0是，1不是

	public User() {

	}

	public User(String id, String user_name_en, String user_name_cn, String email, String password, String group_id,
			List<String> role_id, String svn_user, String redmine_user, String git_user_id, String git_user,
			String git_token, String telephone, String company_id, String status, String is_once_login,
			List<String> labels, String create_date, String leave_date, String area_id, String function_id,
			String rank_id, String education, String start_time, String remark, String is_party_member, String section,
			String work_num, String is_manage_group,String deploy_white) {
		this.id = id;
		this.user_name_en = user_name_en;
		this.user_name_cn = user_name_cn;
		this.email = email;
		this.password = password;
		this.group_id = group_id;
		this.role_id = role_id;
		this.svn_user = svn_user;
		this.redmine_user = redmine_user;
		this.git_user_id = git_user_id;
		this.git_user = git_user;
		this.git_token = git_token;
		this.telephone = telephone;
		this.company_id = company_id;
		this.status = status;
		this.is_once_login = is_once_login;
		this.labels = labels;
		this.create_date = create_date;
		this.leave_date = leave_date;
		this.area_id = area_id;
		this.function_id = function_id;
		this.rank_id = rank_id;
		this.education = education;
		this.start_time = start_time;
		this.remark = remark;
		this.is_party_member = is_party_member;
		this.section = section;
		this.work_num = work_num;
		this.is_manage_group = is_manage_group;
		this.deploy_white = deploy_white;
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

	public String getUser_name_en() {
		return user_name_en;
	}

	public void setUser_name_en(String user_name_en) {
		this.user_name_en = user_name_en;
	}

	public String getUser_name_cn() {
		return user_name_cn;
	}

	public void setUser_name_cn(String user_name_cn) {
		this.user_name_cn = user_name_cn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getGroup_id() {
		return group_id;
	}

	public void setGroup_id(String group_id) {
		this.group_id = group_id;
	}

	public List<String> getRole_id() {
		return role_id;
	}

	public void setRole_id(List<String> role_id) {
		this.role_id = role_id;
	}

	public String getSvn_user() {
		return svn_user;
	}

	public void setSvn_user(String svn_user) {
		this.svn_user = svn_user;
	}

	public String getRedmine_user() {
		return redmine_user;
	}

	public void setRedmine_user(String redmine_user) {
		this.redmine_user = redmine_user;
	}

	public String getGit_user_id() {
		return git_user_id;
	}

	public void setGit_user_id(String git_user_id) {
		this.git_user_id = git_user_id;
	}

	public String getGit_user() {
		return git_user;
	}

	public void setGit_user(String git_user) {
		this.git_user = git_user;
	}

	public String getGit_token() {
		return git_token;
	}

	public void setGit_token(String git_token) {
		this.git_token = git_token;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getCompany_id() {
		return company_id;
	}

	public void setCompany_id(String company_id) {
		this.company_id = company_id;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIs_once_login() {
		return is_once_login;
	}

	public void setIs_once_login(String is_once_login) {
		this.is_once_login = is_once_login;
	}

	public List<String> getLabels() {
		return labels;
	}

	public void setLabels(List<String> labels) {
		this.labels = labels;
	}

	public String getCreate_date() {
		return create_date;
	}

	public void setCreate_date(String create_date) {
		this.create_date = create_date;
	}

	public String getLeave_date() {
		return leave_date;
	}

	public void setLeave_date(String leave_date) {
		this.leave_date = leave_date;
	}

	public String getArea_id() {
		return area_id;
	}

	public void setArea_id(String area_id) {
		this.area_id = area_id;
	}

	public String getFunction_id() {
		return function_id;
	}

	public void setFunction_id(String function_id) {
		this.function_id = function_id;
	}

	public String getRank_id() {
		return rank_id;
	}

	public void setRank_id(String rank_id) {
		this.rank_id = rank_id;
	}

	public String getEducation() {
		return education;
	}

	public void setEducation(String education) {
		this.education = education;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getVm_ip() {
		return vm_ip;
	}

	public void setVm_ip(String vm_ip) {
		this.vm_ip = vm_ip;
	}

	public String getVm_name() {
		return vm_name;
	}

	public void setVm_name(String vm_name) {
		this.vm_name = vm_name;
	}

	public String getVm_user_name() {
		return vm_user_name;
	}

	public void setVm_user_name(String vm_user_name) {
		this.vm_user_name = vm_user_name;
	}

	public String getPhone_type() {
		return phone_type;
	}

	public void setPhone_type(String phone_type) {
		this.phone_type = phone_type;
	}

	public String getPhone_mac() {
		return phone_mac;
	}

	public void setPhone_mac(String phone_mac) {
		this.phone_mac = phone_mac;
	}

	public Boolean getKf_approval() {
		return kf_approval;
	}

	public void setKf_approval(Boolean kf_approval) {
		this.kf_approval = kf_approval;
	}

	public Boolean getNet_move() {
		return net_move;
	}

	public void setNet_move(Boolean net_move) {
		this.net_move = net_move;
	}

	public Boolean getIs_spdb() {
		return is_spdb;
	}

	public void setIs_spdb(Boolean is_spdb) {
		this.is_spdb = is_spdb;
	}

	public String getIs_spdb_mac() {
		return is_spdb_mac;
	}

	public void setIs_spdb_mac(String is_spdb_mac) {
		this.is_spdb_mac = is_spdb_mac;
	}

	public Boolean getIs_kfApproval() {
		return is_kfApproval;
	}

	public void setIs_kfApproval(Boolean is_kfApproval) {
		this.is_kfApproval = is_kfApproval;
	}

	public Boolean getIs_vmApproval() {
		return is_vmApproval;
	}

	public void setIs_vmApproval(Boolean is_vmApproval) {
		this.is_vmApproval = is_vmApproval;
	}

	public String getFtms_level() {
		return ftms_level;
	}

	public void setFtms_level(String ftms_level) {
		this.ftms_level = ftms_level;
	}

	public String getMantis_token() {
		return mantis_token;
	}

	public void setMantis_token(String mantis_token) {
		this.mantis_token = mantis_token;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getIs_party_member() {
		return is_party_member;
	}

	public void setIs_party_member(String is_party_member) {
		this.is_party_member = is_party_member;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public String getWork_num() {
		return work_num;
	}

	public void setWork_num(String work_num) {
		this.work_num = work_num;
	}

	public String getIs_manage_group() {
		return is_manage_group;
	}

	public void setIs_manage_group(String is_manage_group) {
		this.is_manage_group = is_manage_group;
	}
	
	public String getDeploy_white() {
		return deploy_white;
	}

	public void setDeploy_white(String deploy_white) {
		this.deploy_white = deploy_white;
	}

	@Override
	public String toString() {
		return "User{" + "id='" + id + '\'' + ", user_name_en='" + user_name_en + '\'' + ", user_name_cn='"
				+ user_name_cn + '\'' + ", email='" + email + '\'' + ", password='" + password + '\'' + ", group_id='"
				+ group_id + '\'' + ", role_id=" + role_id + ", svn_user='" + svn_user + '\'' + ", redmine_user='"
				+ redmine_user + '\'' + ", git_user_id='" + git_user_id + '\'' + ", git_user='" + git_user + '\''
				+ ", git_token='" + git_token + '\'' + ", telephone='" + telephone + '\'' + ", kf_approval="
				+ kf_approval + ", net_move=" + net_move + ", is_spdb=" + is_spdb + ", vm_ip='" + vm_ip + '\''
				+ ", vm_name='" + vm_name + '\'' + ", vm_user_name='" + vm_user_name + '\'' + ", phone_type='"
				+ phone_type + '\'' + ", phone_mac='" + phone_mac + '\'' + ", is_kfApproval=" + is_kfApproval
				+ ", is_vmApproval=" + is_vmApproval + ", is_spdb_mac='" + is_spdb_mac + '\'' + ", company_id='"
				+ company_id + '\'' + ", status='" + status + '\'' + ", is_once_login='" + is_once_login + '\''
				+ ", labels=" + labels + ", create_date='" + create_date + '\'' + ", leave_date='" + leave_date + '\''
				+ ", area_id='" + area_id + '\'' + ", function_id='" + function_id + '\'' + ", rank_id='" + rank_id
				+ '\'' + ", education='" + education + '\'' + ", start_time='" + start_time + '\'' + ", remark='"
				+ remark + '\'' + ", ftms_level='" + ftms_level + '\'' + ", mantis_token='" + mantis_token + '\''
				+ ", createTime='" + createTime + '\'' + ", updateTime='" + updateTime + '\'' + ", is_party_member="
				+ is_party_member + ", section='" + section + '\'' + ", work_num='" + work_num + '\''
				+ ", is_manage_group='" + is_manage_group + '\'' +    ", deploy_white='"  +  deploy_white +  '}' ;
	}

	// 存数据时，初始化_id和id
	public void initId() {
		ObjectId temp = new ObjectId();
		this._id = temp;
		this.id = temp.toString();
	}

}
