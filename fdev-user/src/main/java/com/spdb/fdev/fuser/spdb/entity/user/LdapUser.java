package com.spdb.fdev.fuser.spdb.entity.user;

import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Document(collection = "LdapUser")
@ApiModel(value = "ldap用户")
public class LdapUser {
	@ApiModelProperty(value = "uid")
	private String uid;
	@ApiModelProperty(value = "sn")
	private String userNameEn;
	@ApiModelProperty(value = "cn")
	private String userNameCn;
	@ApiModelProperty(value = "密码")
	private String password;
	@ApiModelProperty(value = "邮箱")
	private String mail;
	@ApiModelProperty(value = "手机号")
	private String mobile;

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUserNameEn() {
		return userNameEn;
	}

	public void setUserNameEn(String userNameEn) {
		this.userNameEn = userNameEn;
	}

	public String getUserNameCn() {
		return userNameCn;
	}

	public void setUserNameCn(String userNameCn) {
		this.userNameCn = userNameCn;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@Override
	public String toString() {
		return "LdapUser [uid=" + uid + ", userNameEn=" + userNameEn + ", userNameCn=" + userNameCn + ", password="
				+ password + ", mail=" + mail + ", mobile=" + mobile + "]";
	}

}
