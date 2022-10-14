package com.spdb.fdev.fuser.spdb.entity.user;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Document(collection = "role")
@ApiModel(value = "角色")
public class Role implements Serializable {

	private static final long serialVersionUID = 2327139297301528549L;

	@Id
	@JsonIgnore
	private ObjectId _id;

	@Field("id")
	@ApiModelProperty(value = "角色ID")
	private String id;

	@Field("name")
	@ApiModelProperty(value = "角色名称")
	private String name;
	
	@ApiModelProperty(value = "角色使用人数")
	private Integer count;
	
	@Field("status")
	@ApiModelProperty(value="角色状态, 0是废弃, 1是使用")
	private String status;

	@Field("functions")
	@ApiModelProperty(value="角色对应功能点")
	private String functions;

	@Field("menus")
	@ApiModelProperty(value="角色对应菜单id集合")
	private List<String> menus;

	public Role() {

	}

	public Role(String id, String name, Integer count, String status, String functions, List menus) {
		this.id = id;
		this.name = name;
		this.count = count;
		this.status = status;
		this.functions = functions;
		this.menus = menus;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void set_id(ObjectId _id) {
		this._id = _id;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public ObjectId get_id() {
		return _id;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getFunctions() {
		return functions;
	}

	public void setFunctions(String functions) {
		this.functions = functions;
	}

	public List<String> getMenus() {
		return menus;
	}

	public void setMenus(List<String> menus) {
		this.menus = menus;
	}

	//存数据时，初始化_id和id
	public void initId() {
		ObjectId temp = new ObjectId();
		this._id = temp;
		this.id = temp.toString();
	}


}
