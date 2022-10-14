package com.fdev.database.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "sysInfo")
@ApiModel(value="系统与库表信息")
public class SysInfo implements Serializable {
	private static final long serialVersionUID = 7528659211794720516L;
	@Id
	private ObjectId _id;

	@Field("sys_id")
	@ApiModelProperty(value="系统与库名关系id")
	private String sys_id;

	@Field("system_name_cn")
	@ApiModelProperty(value="系统中文名称")
	private String system_name_cn;

	@Field("database_name")
	@ApiModelProperty(value="库名")
	private String database_name;

	public String getSys_id() {
		return sys_id;
	}

	public void setSys_id(String sys_id) {
		this.sys_id = sys_id;
	}

	public String getSystem_name_cn() {
		return system_name_cn;
	}

	public void setSystem_name_cn(String system_name_cn) {
		this.system_name_cn = system_name_cn;
	}

	public String getDatabase_name() {
		return database_name;
	}

	public void setDatabase_name(String database_name) {
		this.database_name = database_name;
	}

	public SysInfo() {

	}

	public SysInfo(String sys_id, String system_name_cn, String database_name) {
		this.sys_id = sys_id;
		this.system_name_cn = system_name_cn;
		this.database_name = database_name;
	}

	@Override
	public String toString() {
		return "SysInfo{" +
				"sys_id='" + sys_id + '\'' +
				", system_name_cn='" + system_name_cn + '\'' +
				", database_name='" + database_name + '\'' +
				'}';
	}

    public void initId() {
        ObjectId temp = new ObjectId();
        this.sys_id=temp.toString();
        this._id=temp;
    }
}
