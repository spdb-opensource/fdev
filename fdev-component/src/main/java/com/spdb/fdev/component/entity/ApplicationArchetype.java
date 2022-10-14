package com.spdb.fdev.component.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;
@Component
@Document(collection = "application_archetype")
public class ApplicationArchetype implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4922484292204015428L;
	@Id
	private ObjectId _id;
	@Field("id")
	private String id;
	@Field("archetype_id")
	private String archetype_id;
	@Field("archetype_version")
	private String archetype_version;
	@Field("application_id")
	private String application_id;
	@Field("update_time")
	private String update_time;

	public ObjectId get_id() {
		return _id;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getArchetype_id() {
		return archetype_id;
	}

	public void setArchetype_id(String archetype_id) {
		this.archetype_id = archetype_id;
	}

	public String getArchetype_version() {
		return archetype_version;
	}

	public void setArchetype_version(String archetype_version) {
		this.archetype_version = archetype_version;
	}

	public String getApplication_id() {
		return application_id;
	}

	public void setApplication_id(String application_id) {
		this.application_id = application_id;
	}

	public String getUpdate_time() {
		return update_time;
	}

	public void setUpdate_time(String update_time) {
		this.update_time = update_time;
	}
}
