package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = Dict.INTERFACE_YAPI_INTERFACES)
public class YapiInterface implements Serializable {

    private static final long serialVersionUID = -2660736719481308797L;

    @Id
    private String id;
    @Field(Dict.PROJECTID)
    private String project_id; //项目id
    //项目token
    @Field(Dict.YAPI_TOKEN)
    private String yapi_token; //项目token
    @Field(Dict.INTERFACE_ID)
    private String interface_id;//接口ID
    @Field(Dict.INTERFACE_NAME)
    private String interface_name; //接口名称
    @Field(Dict.INTERFACE_PATH)
    private String interface_path;//接口地址
    @Field(Dict.INTERFACE_JSON_SCHEMA)
    private String json_schema;//此接口的jsonSchema
    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String create_time;
    @Field(Dict.UPDATE_TIME)
    private String update_time;//接口更新时间

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getYapi_token() {
        return yapi_token;
    }

    public void setYapi_token(String yapi_token) {
        this.yapi_token = yapi_token;
    }

    public String getInterface_id() {
        return interface_id;
    }

    public void setInterface_id(String interface_id) {
        this.interface_id = interface_id;
    }

    public String getInterface_name() {
        return interface_name;
    }

    public void setInterface_name(String interface_name) {
        this.interface_name = interface_name;
    }

    public String getInterface_path() {
        return interface_path;
    }

    public void setInterface_path(String interface_path) {
        this.interface_path = interface_path;
    }

    public String getJson_schema() {
        return json_schema;
    }

    public void setJson_schema(String json_schema) {
        this.json_schema = json_schema;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "YapiInterface{" +
                "id='" + id + '\'' +
                ", project_id='" + project_id + '\'' +
                ", yapi_token='" + yapi_token + '\'' +
                ", interface_id='" + interface_id + '\'' +
                ", interface_name='" + interface_name + '\'' +
                ", interface_path='" + interface_path + '\'' +
                ", json_schema='" + json_schema + '\'' +
                ", create_time='" + create_time + '\'' +
                ", update_time='" + update_time + '\'' +
                '}';
    }
}
