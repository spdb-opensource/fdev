package com.spdb.fdev.fdevapp.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: luotao
 * Date: 2019/2/28
 * Time: 下午2:00
 * To change this template use File | Settings | File Templates.
 */
@Document(collection = "app-deploy-net")
@ApiModel(value="应用部署网段")
public class DeployNet implements Serializable{

	@Id
    @ApiModelProperty(value="ObjectId")
    private ObjectId _id;
 

    @Field("id")
    @ApiModelProperty(value="ObjectId")
    private String id;
    

	@Field("name")
    @ApiModelProperty(value="业务网段")
    private String name;

	public DeployNet() {
	}

	public DeployNet(ObjectId _id, String id, String name) {
		this._id = _id;
		this.id = id;
		this.name = name;
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "DeployNet{" +
				"_id=" + _id +
				", id='" + id + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
