package com.spdb.fdev.fuser.spdb.entity.user;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.annotations.ApiModelProperty;

@Document(collection = "netApproval")
@ApiModel(value="网络审核")
public class NetApproval implements Serializable {

	private static final long serialVersionUID = 8755872801785022712L;

	@Id
	private ObjectId _id;

	@Field("id")
	@ApiModelProperty(value="记录id")
	private String id;
	
	@Field("type")
	@ApiModelProperty(value="审核类型")
	private String type;
	
	@Field("applicant_id")
	@ApiModelProperty(value="申请人id")
	private String applicant_id;
	
	@Field("user_id")
	@ApiModelProperty(value="使用人id")
	private String user_id;
	
	@Field("phone_type")
	@ApiModelProperty(value="手机型号")
	private String phone_type;
	
	@Field("phone_mac")
	@ApiModelProperty(value="手机mac地址")
	private String phone_mac;

	@Field("vm_ip")
	@ApiModelProperty(value="虚机ip")
	private String vm_ip;
	
	@Field("vm_name")
	@ApiModelProperty(value="虚机名")
	private String vm_name;
	
	@Field("vm_user_name")
	@ApiModelProperty(value="虚机用户名")
	private String vm_user_name;
	
	//默认状态为待审核 wait_approve
	@Field("status")
	@ApiModelProperty(value="审核状态")
	private String status;
	
	//KF白名单开通审核：默认为0， 发送邮件提醒后改为2，每过一天增加1，达到4以后关闭
	@Field("off_flag")
	@ApiModelProperty(value="审核关闭标志")
	private int off_flag;

	@Field("create_time")
	@ApiModelProperty(value="申请时间")
	private String create_time;
	
	@Field("update_time")
	@ApiModelProperty(value="更新时间")
	private String update_time;
	
	/**
	 * @return the phone_type
	 */
	public String getPhone_type() {
		return phone_type;
	}

	/**
	 * @param phone_type the phone_type to set
	 */
	public void setPhone_type(String phone_type) {
		this.phone_type = phone_type;
	}

	/**
	 * @return the phone_mac
	 */
	public String getPhone_mac() {
		return phone_mac;
	}
	
	/**
	 * @return the off_flag
	 */
	public int getOff_flag() {
		return off_flag;
	}

	/**
	 * @param off_flag the off_flag to set
	 */
	public void setOff_flag(int off_flag) {
		this.off_flag = off_flag;
	}

	/**
	 * @param phone_mac the phone_mac to set
	 */
	public void setPhone_mac(String phone_mac) {
		this.phone_mac = phone_mac;
	}

	/**
	 * @return the vm_ip
	 */
	public String getVm_ip() {
		return vm_ip;
	}

	/**
	 * @param vm_ip the vm_ip to set
	 */
	public void setVm_ip(String vm_ip) {
		this.vm_ip = vm_ip;
	}

	/**
	 * @return the vm_name
	 */
	public String getVm_name() {
		return vm_name;
	}

	/**
	 * @param vm_name the vm_name to set
	 */
	public void setVm_name(String vm_name) {
		this.vm_name = vm_name;
	}

	/**
	 * @return the vm_user_name
	 */
	public String getVm_user_name() {
		return vm_user_name;
	}

	/**
	 * @param vm_user_name the vm_user_name to set
	 */
	public void setVm_user_name(String vm_user_name) {
		this.vm_user_name = vm_user_name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the applicant_id
	 */
	public String getApplicant_id() {
		return applicant_id;
	}

	/**
	 * @param applicant_id the applicant_id to set
	 */
	public void setApplicant_id(String applicant_id) {
		this.applicant_id = applicant_id;
	}

	/**
	 * @return the user_id
	 */
	public String getUser_id() {
		return user_id;
	}

	/**
	 * @param user_id the user_id to set
	 */
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the create_time
	 */
	public String getCreate_time() {
		return create_time;
	}

	/**
	 * @param create_time the create_time to set
	 */
	public void setCreate_time(String create_time) {
		this.create_time = create_time;
	}

	/**
	 * @return the update_time
	 */
	public String getUpdate_time() {
		return update_time;
	}

	/**
	 * @param update_time the update_time to set
	 */
	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}
	
}
