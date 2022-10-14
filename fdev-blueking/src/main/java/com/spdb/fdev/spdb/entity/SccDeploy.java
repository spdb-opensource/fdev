package com.spdb.fdev.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Author:guanz2
 * @Date:2021/10/3-17:02
 * @Description: scc deployment 实体类
 */
@Document(collection = Dict.SCCDEPLOY)
public class SccDeploy {

    private static final long serialVersionUID = 8637314009320661714L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("resource_code")
    private String resource_code;

    @Field("namespace_code")
    private String namespace_code;

    @Field("deploy_create_time")
    private String deploy_create_time;

    @Field("yaml")
    private String yaml;

    @Field("last_modified_date")
    private String last_modified_date;

    public SccDeploy(){

    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getResource_code() {
        return resource_code;
    }

    public void setResource_code(String resource_code) {
        this.resource_code = resource_code;
    }

    public String getNamespace_code() {
        return namespace_code;
    }

    public void setNamespace_code(String namespace_code) {
        this.namespace_code = namespace_code;
    }

    public String getDeploy_create_time() {
        return deploy_create_time;
    }

    public void setDeploy_create_time(String deploy_create_time) {
        this.deploy_create_time = deploy_create_time;
    }

    public String getYaml() {
        return yaml;
    }

    public void setYaml(String yaml) {
        this.yaml = yaml;
    }

    public String getLast_modified_date() {
        return last_modified_date;
    }

    public void setLast_modified_date(String last_modified_date) {
        this.last_modified_date = last_modified_date;
    }

    @Override
    public String toString() {
        return "SccDeploy{" +
                "_id=" + _id +
                ", resource_code='" + resource_code + '\'' +
                ", namespace_code='" + namespace_code + '\'' +
                ", deploy_create_time='" + deploy_create_time + '\'' +
                ", yaml='" + yaml + '\'' +
                ", last_modified_date='" + last_modified_date + '\'' +
                '}';
    }
}
