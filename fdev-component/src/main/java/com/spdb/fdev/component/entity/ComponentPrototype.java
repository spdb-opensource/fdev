package com.spdb.fdev.component.entity;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;
@Component
@Document(collection = "component_prototype")
public class ComponentPrototype implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8859672319775292243L;
	
	@Field("prototype_id")
	private String prototype_id;
	@Field("component_id")
	private String component_id;
	@Field("prototype_version")
	private String component_version;
	@Field("isLatest")
	private String isLatest;
	@Field("update_time")
	private String update_time;
	public String getPrototype_id() {
		return prototype_id;
	}
	public void setPrototype_id(String prototype_id) {
		this.prototype_id = prototype_id;
	}
	public String getComponent_id() {
		return component_id;
	}
	public void setComponent_id(String component_id) {
		this.component_id = component_id;
	}
	public String getComponent_version() {
		return component_version;
	}
	public void setComponent_version(String component_version) {
		this.component_version = component_version;
	}
	public String getIsLatest() {
		return isLatest;
	}
	public void setIsLatest(String isLatest) {
		this.isLatest = isLatest;
	}
	public String getUpdate_time() {
		return update_time;
	}
	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}
	
	

}
