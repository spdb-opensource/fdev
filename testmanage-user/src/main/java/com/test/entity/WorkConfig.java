package com.test.entity;

import java.io.Serializable;

public class WorkConfig implements Serializable{
	
	private String id;
	
	private String ftms_group_id;
	
	private String work_leader;
	
	private String group_leader;
	
	private String group_name;
	
	private String groupManager;
	
	private String groupLeaders;

	private String uatContact;

	private String uatContactName;

	private String securityLeader;

	public String getGroup_name() {
		return group_name;
	}

	public void setGroup_name(String group_name) {
		this.group_name = group_name;
	}

	public String getGroupManager() {
		return groupManager;
	}

	public void setGroupManager(String groupManager) {
		this.groupManager = groupManager;
	}

	public String getGroupLeaders() {
		return groupLeaders;
	}

	public void setGroupLeaders(String groupLeaders) {
		this.groupLeaders = groupLeaders;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFtms_group_id() {
		return ftms_group_id;
	}

	public void setFtms_group_id(String ftms_group_id) {
		this.ftms_group_id = ftms_group_id;
	}

	public String getWork_leader() {
		return work_leader;
	}

	public void setWork_leader(String work_leader) {
		this.work_leader = work_leader;
	}

	public String getGroup_leader() {
		return group_leader;
	}

	public void setGroup_leader(String group_leader) {
		this.group_leader = group_leader;
	}

	public String getUatContact() { return uatContact; }

	public void setUatContact(String uatContact) { this.uatContact = uatContact; }

	public String getUatContactName() { return uatContactName; }

	public void setUatContactName(String uatContactName) { this.uatContactName = uatContactName; }

	public String getSecurityLeader() {
		return securityLeader;
	}

	public void setSecurityLeader(String securityLeader) {
		this.securityLeader = securityLeader;
	}
}
