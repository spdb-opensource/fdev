package com.fdev.database.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.*;



@Document(collection = "appinfo")
@ApiModel(value="应用信息")
public class App implements Serializable {
	private static final long serialVersionUID = 7528659211794720516L;
	@Id
	private ObjectId _id;

	@Field("id")
	@ApiModelProperty(value="应用信息id")
	private String id;

	@Field("appName_en")
	@ApiModelProperty(value="应用英文名称")
	private String appName_en;

	@Field("appName_cn")
	@ApiModelProperty(value="应用中文名称")
	private String appName_cn;

	@Field("spdb_managers")
	@ApiModelProperty(value = "行内负责人")
	private List<Map> spdb_managers;

	public App() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppName_en() {
		return appName_en;
	}

	public void setAppName_en(String appName_en) {
		this.appName_en = appName_en;
	}

	public String getAppName_cn() {
		return appName_cn;
	}

	public void setAppName_cn(String appName_cn) {
		this.appName_cn = appName_cn;
	}

	public List<Map> getSpdb_managers() {
		return spdb_managers;
	}

	public void setSpdb_managers(List<Map> spdb_managers) {
		this.spdb_managers = spdb_managers;
	}

	public App(String id, String appName_en, String appName_cn, List<Map> spdb_managers) {
		this.id = id;
		this.appName_en = appName_en;
		this.appName_cn = appName_cn;
		this.spdb_managers = spdb_managers;
	}

	@Override
	public String toString() {
		return "App{" +
				"id='" + id + '\'' +
				", appName_en='" + appName_en + '\'' +
				", appName_cn='" + appName_cn + '\'' +
				", spdb_managers=" + spdb_managers +
				'}';
	}
}
