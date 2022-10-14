package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = Dict.INTERFACE_YAPI_PROJECT)
public class YapiProject implements Serializable {
    private static final long serialVersionUID = -8784403365734213254L;
    @Id
    private String id;


    //项目id
    @Field(Dict.PROJECTID)
    private String project_id;


    //项目名称
    @Field(Dict.PROJECT_NAME)
    private String project_name;


    //项目token
    @Field(Dict.YAPI_TOKEN)
    private String yapi_token;


    //导入人
    @Field(Dict.IMPORT_USER)
    private Map import_user;


    //接口列表
    @Field(Dict.L_INTERFACES)
    private List<YapiInterface> interfaces;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String create_time;

    // 更新时间
    @Field(Dict.UPDATE_TIME)
    private String update_time;

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

    public String getProject_name() {
        return project_name;
    }

    public void setProject_name(String project_name) {
        this.project_name = project_name;
    }

    public String getYapi_token() {
        return yapi_token;
    }

    public void setYapi_token(String yapi_token) {
        this.yapi_token = yapi_token;
    }

    public Map getImport_user() {
        return import_user;
    }

    public void setImport_user(Map import_user) {
        this.import_user = import_user;
    }

    public List<YapiInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<YapiInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }


}
