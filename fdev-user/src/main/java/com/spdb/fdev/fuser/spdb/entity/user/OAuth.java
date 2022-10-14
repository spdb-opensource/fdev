package com.spdb.fdev.fuser.spdb.entity.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@ApiModel(value = "第三方认证")
@Document(collection = "oAuth")
public class OAuth {
    @Id
    private ObjectId _id;
    @Field
    @ApiModelProperty(value = "id")
    private String id;
    @Field
    @ApiModelProperty(value = "名字")
    private String name;
    @Field
    @ApiModelProperty(value = "地址")
    private String host;

    public OAuth() {
    }

    public OAuth(String id, String name, String host) {
        this.id = id;
        this.name = name;
        this.host = host;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }
    public String getHost() {
        return host;
    }

	public ObjectId get_id() {
		return _id;
	}
    


	//存数据时，初始化_id和id
	public void initId() {
		ObjectId temp = new ObjectId();
		this._id = temp;
		this.id = temp.toString();
	}

}
