package com.spdb.fdev.fdevapp.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = "service-system")
@ApiModel(value="服务系统")
public class ServiceSystem implements Serializable {
    private static final long serialVersionUID = -9189169943794059732L;
    @Id
    @JsonIgnore
    private Object _id;

    @Field(value = "id")
    @ApiModelProperty(value = "系统id")
    private String id;

    @Field(value = "name_en")
    @ApiModelProperty(value = "系统英文名")
    private  String name_en;

    @Field(value = "name_cn")
    @ApiModelProperty(value = "系统中文名")
    private  String name_cn;

	public Object get_id() {
		return _id;
	}

	public void set_id(Object _id) {
		this._id = _id;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName_en() {
		return name_en;
	}

	public void setName_en(String name_en) {
		this.name_en = name_en;
	}

	public String getName_cn() {
		return name_cn;
	}

	public void setName_cn(String name_cn) {
		this.name_cn = name_cn;
	}
    
}
