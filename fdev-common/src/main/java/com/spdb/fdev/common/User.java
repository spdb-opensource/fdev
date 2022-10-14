package com.spdb.fdev.common;

import java.io.Serializable;
import java.util.List;


public class User implements Serializable {

	private static final long serialVersionUID = -5382360368973552125L;

	private String id;
	
	private String user_name_en;//用户英文名称
	
	private String user_name_cn;//用户中文名称
	
	private String email;//用户邮箱
	
	private String password;//用户密码   
	
	private String group_id;//group{ id:"id" , groupName : "name" }
	
	private List<String> role_id;//角色
	
	private String permission_id; //权限
	
	private String svn_user;//svn
	
	private String redmine_user;//redmine

	private String git_user_id;//git_user_id
	
	private String git_user;//gitlab
	
	private String git_token;//gitlabToken
	
	private String telephone;//手机号
	
	private String company_id;//公司
	
	private String status;//状态  在职0     离职1
	
	private String is_once_login;//是否首次登录  0 是
	
	private List<String>  labels;//用户标签

	private String  deploy_white;//部署白名单
	
	public User() {
		
	}

	public User(String id, String user_name_en, String user_name_cn, String email, String password, String group_id, List<String> role_id, String permission_id, String svn_user, String redmine_user, String git_user_id, String git_user, String git_token, String telephone, String company_id, String status, String is_once_login, List<String> labels,String deploy_white) {
		this.id = id;
		this.user_name_en = user_name_en;
		this.user_name_cn = user_name_cn;
		this.email = email;
		this.password = password;
		this.group_id = group_id;
		this.role_id = role_id;
		this.permission_id = permission_id;
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
		this.deploy_white = deploy_white;
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

	public String getPermission_id() {
		return permission_id;
	}

	public void setPermission_id(String permission_id) {
		this.permission_id = permission_id;
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

	public String getGit_user_id() { return git_user_id; }

	public void setGit_user_id(String git_user_id) { this.git_user_id = git_user_id; }
	
	public String getDeploy_white() {
		return deploy_white;
	}

	public void setDeploy_white(String deploy_white) {
		this.deploy_white = deploy_white;
	}

	@Override
	public String toString() {
		return "User{" +
				"id='" + id + '\'' +
				", user_name_en='" + user_name_en + '\'' +
				", user_name_cn='" + user_name_cn + '\'' +
				", email='" + email + '\'' +
				", password='" + password + '\'' +
				", group_id='" + group_id + '\'' +
				", role_id=" + role_id +
				", permission_id='" + permission_id + '\'' +
				", svn_user='" + svn_user + '\'' +
				", redmine_user='" + redmine_user + '\'' +
				", git_user_id='" + git_user_id + '\'' +
				", git_user='" + git_user + '\'' +
				", git_token='" + git_token + '\'' +
				", telephone='" + telephone + '\'' +
				", company_id='" + company_id + '\'' +
				", status='" + status + '\'' +
				", is_once_login='" + is_once_login + '\'' +
				", labels=" + labels + '\'' +
				", deploy_white=" + deploy_white + '\'' +
				'}';
	}
}
