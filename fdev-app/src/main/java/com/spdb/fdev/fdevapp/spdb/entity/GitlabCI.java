package com.spdb.fdev.fdevapp.spdb.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Document(collection = "app-gitlabci")
@ApiModel(value="持续集成模版实体")
public class GitlabCI implements Serializable{
	
    @Id
    @ApiModelProperty(value="ID")
    private ObjectId _id;
    
    @Field("id")
    @ApiModelProperty(value="id")
    private String id;

    @Field("name")
    @ApiModelProperty(value="vue 持续集成模板")
    private String name;
    
    @Field("yaml_name")
    @ApiModelProperty(value="gitlab-ci.yaml")
    private String yaml_name;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYaml_name() {
		return yaml_name;
	}

	public void setYaml_name(String yaml_name) {
		this.yaml_name = yaml_name;
	}

	
    
}
