package com.fdev.database.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "dictRecord")
@ApiModel(value="数据字典记录表（不关联表名）")
public class DictRecord implements Serializable {
	private static final long serialVersionUID = 7528659211794720516L;
	@Id
	private ObjectId _id;

	@Field("record_id")
	@ApiModelProperty(value="字典记录id")
	private String record_id;

	@Field("sys_id")
	@ApiModelProperty(value="关联系统id")
	private String sys_id;

	@Field("dict_id")
	@ApiModelProperty(value="数据字典关联id")
	private String dict_id;

	@Field("is_list_field")
	@ApiModelProperty(value="是否列表字段")
	private String is_list_field;

	@Field("list_field_desc")
	@ApiModelProperty(value="列表字段枚举说明")
	private String list_field_desc;

	@Field("remark")
	@ApiModelProperty(value="备注")
	private String remark;

	@Field("first_modi_time")
	@ApiModelProperty(value="首次维护日期")
	private String first_modi_time;

	@Field("first_modi_userNameEn")
	@ApiModelProperty(value="首次维护人英文名")
	private String first_modi_userNameEn;

	@Field("first_modi_userName")
	@ApiModelProperty(value="首次维护人中文名")
	private String first_modi_userName;

	@Field("last_modi_time")
	@ApiModelProperty(value="最后维护日期")
	private String last_modi_time;

	@Field("last_modi_userNameEn")
	@ApiModelProperty(value="最后维护人英文名")
	private String last_modi_userNameEn;

	@Field("last_modi_userName")
	@ApiModelProperty(value="最后维护人中文名")
	private String last_modi_userName;

	@Field("status")
	@ApiModelProperty(value="记录状态, 0是废弃, 1是使用")
	private String status;

	public String getRecord_id() {
		return record_id;
	}

	public void setRecord_id(String record_id) {
		this.record_id = record_id;
	}

	public String getSys_id() {
		return sys_id;
	}

	public void setSys_id(String sys_id) {
		this.sys_id = sys_id;
	}

	public String getDict_id() {
		return dict_id;
	}

	public void setDict_id(String dict_id) {
		this.dict_id = dict_id;
	}

	public String getIs_list_field() {
		return is_list_field;
	}

	public void setIs_list_field(String is_list_field) {
		this.is_list_field = is_list_field;
	}

	public String getList_field_desc() {
		return list_field_desc;
	}

	public void setList_field_desc(String list_field_desc) {
		this.list_field_desc = list_field_desc;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getFirst_modi_time() {
		return first_modi_time;
	}

	public void setFirst_modi_time(String first_modi_time) {
		this.first_modi_time = first_modi_time;
	}

	public String getFirst_modi_userNameEn() {
		return first_modi_userNameEn;
	}

	public void setFirst_modi_userNameEn(String first_modi_userNameEn) {
		this.first_modi_userNameEn = first_modi_userNameEn;
	}

	public String getFirst_modi_userName() {
		return first_modi_userName;
	}

	public void setFirst_modi_userName(String first_modi_userName) {
		this.first_modi_userName = first_modi_userName;
	}

	public String getLast_modi_time() {
		return last_modi_time;
	}

	public void setLast_modi_time(String last_modi_time) {
		this.last_modi_time = last_modi_time;
	}

	public String getLast_modi_userNameEn() {
		return last_modi_userNameEn;
	}

	public void setLast_modi_userNameEn(String last_modi_userNameEn) {
		this.last_modi_userNameEn = last_modi_userNameEn;
	}

	public String getLast_modi_userName() {
		return last_modi_userName;
	}

	public void setLast_modi_userName(String last_modi_userName) {
		this.last_modi_userName = last_modi_userName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public DictRecord() {

	}

	public DictRecord(String record_id, String sys_id, String dict_id, String is_list_field, String list_field_desc, String remark, String first_modi_time, String first_modi_userNameEn, String first_modi_userName, String last_modi_time, String last_modi_userNameEn, String last_modi_userName, String status) {
		this.record_id = record_id;
		this.sys_id = sys_id;
		this.dict_id = dict_id;
		this.is_list_field = is_list_field;
		this.list_field_desc = list_field_desc;
		this.remark = remark;
		this.first_modi_time = first_modi_time;
		this.first_modi_userNameEn = first_modi_userNameEn;
		this.first_modi_userName = first_modi_userName;
		this.last_modi_time = last_modi_time;
		this.last_modi_userNameEn = last_modi_userNameEn;
		this.last_modi_userName = last_modi_userName;
		this.status = status;
	}

	@Override
	public String toString() {
		return "DictRecord{" +
				"record_id='" + record_id + '\'' +
				", sys_id='" + sys_id + '\'' +
				", dict_id='" + dict_id + '\'' +
				", is_list_field='" + is_list_field + '\'' +
				", list_field_desc='" + list_field_desc + '\'' +
				", remark='" + remark + '\'' +
				", first_modi_time='" + first_modi_time + '\'' +
				", first_modi_userNameEn='" + first_modi_userNameEn + '\'' +
				", first_modi_userName='" + first_modi_userName + '\'' +
				", last_modi_time='" + last_modi_time + '\'' +
				", last_modi_userNameEn='" + last_modi_userNameEn + '\'' +
				", last_modi_userName='" + last_modi_userName + '\'' +
				", status='" + status + '\'' +
				'}';
	}

	public void initId() {
		ObjectId temp = new ObjectId();
		this.record_id=temp.toString();
		this._id=temp;
	}

}
