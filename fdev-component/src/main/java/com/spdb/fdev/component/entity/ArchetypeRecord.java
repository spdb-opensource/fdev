package com.spdb.fdev.component.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;
@Component
@Document(collection = "archetype_record")
public class ArchetypeRecord implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8511240314885682341L;

	@Id
	@JsonIgnore
	private ObjectId _id;
	@Field("id")
	private String id;
	@Field("archetype_id")
	private String archetype_id;
	@Field("version")
	private String version;
	@Field("date")
	private String date;
	@Field("update_user")
	private String update_user;
	@Field("release_log")
	private String release_log;
	@Field("type")
	private String type;
	@Field("packagetype")
	private String packagetype;
	@Field("pipid")
	private String pipid;
	
	public ArchetypeRecord() {
		super();
	}

	public ArchetypeRecord(String archetype_id, String version, String date) {
		this.archetype_id = archetype_id;
		this.version = version;
		this.date = date;
	}

	
	public ArchetypeRecord(String archetype_id, String version, String date, String update_user, String release_log, String type, String packagetype, String pipid) {
		this.archetype_id = archetype_id;
		this.version = version;
		this.date = date;
		this.update_user = update_user;
		this.release_log = release_log;
		this.type = type;
		this.packagetype = packagetype;
		this.pipid = pipid;
	}



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

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getUpdate_user() {
		return update_user;
	}

	public void setUpdate_user(String update_user) {
		this.update_user = update_user;
	}

	public String getRelease_log() {
		return release_log;
	}

	public void setRelease_log(String release_log) {
		this.release_log = release_log;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPackagetype() {
		return packagetype;
	}

	public void setPackagetype(String packagetype) {
		this.packagetype = packagetype;
	}

	public String getPipid() {
		return pipid;
	}

	public void setPipid(String pipid) {
		this.pipid = pipid;
	}
}
