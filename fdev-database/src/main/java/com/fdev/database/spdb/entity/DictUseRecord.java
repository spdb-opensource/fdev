package com.fdev.database.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.*;

/**
 * 记录库表中字段创建后及后续添加字段的情况
 */
@Document(collection = "dictUseRecord")
@ApiModel(value="数据字典登记表")
public class DictUseRecord implements Serializable {
	private static final long serialVersionUID = 7528659211794720516L;
	@Id
	private ObjectId _id;

	@Field("useRecord_id")
	@ApiModelProperty(value="字典登记表id")
	private String useRecord_id;

	@Field("sys_id")
	@ApiModelProperty(value="关联系统id")
	private String sys_id;
	
	@Field("database_type")
	@ApiModelProperty(value="数据库类型")
	private String database_type;

	@Field("database_name")
	@ApiModelProperty(value="库名")
	private String database_name;
	
	@Field("table_name")
	@ApiModelProperty(value="表名")
	private String table_name;

	@Field("all_field")
	@ApiModelProperty(value="表中所有的字段及类型")
	private List<Map> all_field;
	
	@Field("new_field")
	@ApiModelProperty(value="新增字段及类型")
	private List<Map> new_field;

	@Field("is_new_table")
	@ApiModelProperty(value="是否为新增表")
	private String is_new_table;

	@Field("remark")
	@ApiModelProperty(value="备注")
	private String remark;

	@Field("first_modi_time")
	@ApiModelProperty(value="首次维护日期")
	private String first_modi_time;

	@Field("first_modi_userNameEn")
	@ApiModelProperty(value="首次维护人ID")
	private String first_modi_userNameEn;

	@Field("first_modi_userName")
	@ApiModelProperty(value="首次维护人姓名")
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
	@ApiModelProperty(value="使用记录状态, 0是废弃, 1是使用")
	private String status;

	public String getUseRecord_id() {
		return useRecord_id;
	}

	public void setUseRecord_id(String useRecord_id) {
		this.useRecord_id = useRecord_id;
	}

	public String getSys_id() {
		return sys_id;
	}

	public void setSys_id(String sys_id) {
		this.sys_id = sys_id;
	}

	public String getDatabase_type() {
		return database_type;
	}

	public void setDatabase_type(String database_type) {
		this.database_type = database_type;
	}

	public String getDatabase_name() {
		return database_name;
	}

	public void setDatabase_name(String database_name) {
		this.database_name = database_name;
	}

	public String getTable_name() {
		return table_name;
	}

	public void setTable_name(String table_name) {
		this.table_name = table_name;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public List<Map> getAll_field() {
		return all_field;
	}

	public void setAll_field(List<Map> all_field) {
		this.all_field = all_field;
	}

	public List<Map> getNew_field() {
		return new_field;
	}

	public void setNew_field(List<Map> new_field) {
		this.new_field = new_field;
	}

	public String getIs_new_table() {
		return is_new_table;
	}

	public void setIs_new_table(String is_new_table) {
		this.is_new_table = is_new_table;
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

	public DictUseRecord() {

	}

	public DictUseRecord(String useRecord_id, String sys_id, String database_type, String database_name, String table_name, List<Map> all_field, List<Map> new_field, String is_new_table, String remark, String first_modi_time, String first_modi_userNameEn, String first_modi_userName, String last_modi_time, String last_modi_userNameEn, String last_modi_userName, String status) {
		this.useRecord_id = useRecord_id;
		this.sys_id = sys_id;
		this.database_type = database_type;
		this.database_name = database_name;
		this.table_name = table_name;
		this.all_field = all_field;
		this.new_field = new_field;
		this.is_new_table = is_new_table;
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
		return "DictUseRecord{" +
				"useRecord_id='" + useRecord_id + '\'' +
				", sys_id='" + sys_id + '\'' +
				", database_type='" + database_type + '\'' +
				", database_name='" + database_name + '\'' +
				", table_name='" + table_name + '\'' +
				", all_field=" + all_field +
				", new_field=" + new_field +
				", is_new_table='" + is_new_table + '\'' +
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
		this.useRecord_id=temp.toString();
		this._id=temp;
	}
}
