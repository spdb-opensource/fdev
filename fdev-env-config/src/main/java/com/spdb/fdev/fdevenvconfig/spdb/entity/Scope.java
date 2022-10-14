package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = "scope")
@ApiModel(value = "作用域")
public class Scope implements Serializable {

    private static final long serialVersionUID = -5015599340847206111L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "作用域id")
    private String id;

    @Field("scope")
    @ApiModelProperty(value = "作用域")
    private List<String> scope;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("utime")
    @ApiModelProperty(value = "修改时间")
    private String utime;

    public Scope() {

    }

    public Scope(ObjectId _id, String id, List<String> scope, String ctime, String utime) {
        super();
        this._id = _id;
        this.id = id;
        this.scope = scope;
        this.ctime = ctime;
        this.utime = utime;
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

    public List<String> getScope() {
        return scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }

    @Override
    public String toString() {
        return "Scope [_id=" + _id + ", id=" + id + ", scope=" + scope + ", ctime=" + ctime + ", utime=" + utime + "]";
    }

}
