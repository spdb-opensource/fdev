package com.manager.ftms.entity;

import java.io.Serializable;

public class PersonReport implements Serializable {

	private static final long serialVersionUID = -5801486617559148510L;

	private String userName; // 用户名
	private String userNameEn; // 用户英文名
	private String total; // 统计工单数
	private String sendDate; // 查询日期
	private String groupName; // 所属小组

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

	public String getUserNameEn() {
		return userNameEn;
	}

	public void setUserNameEn(String userNameEn) {
		this.userNameEn = userNameEn;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	@Override
	public String toString() {
		return "PersonReport [userName=" + userName + ", userNameEn=" + userNameEn + ", total=" + total + ", sendDate="
				+ sendDate + ", groupName=" + groupName + "]";
	}
}
