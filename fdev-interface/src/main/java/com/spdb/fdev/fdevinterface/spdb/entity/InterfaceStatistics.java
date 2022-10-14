package com.spdb.fdev.fdevinterface.spdb.entity;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.spdb.fdev.fdevinterface.base.dict.Dict;

@Document(collection = Dict.INTERFACE_STATISTICS)
public class InterfaceStatistics implements Serializable{

	private static final long serialVersionUID = -5992700258882282593L;
	
	@Id
	private String id;
	
	//调用方应用名
	@Field(value=Dict.TARGET_SERVICE_NAME)
	private String targetServiceName;
	
	//提供方应用名
	@Field(value=Dict.SOURCE_SERVICE_NAME)
	private String sourceServiceName;
	
	//接口名称
	@Field(value=Dict.NAME)
	private String name;
	
	//接口url
	@Field(value=Dict.URL)
	private String url;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTargetServiceName() {
		return targetServiceName;
	}

	public void setTargetServiceName(String targetServiceName) {
		this.targetServiceName = targetServiceName;
	}

	public String getSourceServiceName() {
		return sourceServiceName;
	}

	public void setSourceServiceName(String sourceServiceName) {
		this.sourceServiceName = sourceServiceName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
