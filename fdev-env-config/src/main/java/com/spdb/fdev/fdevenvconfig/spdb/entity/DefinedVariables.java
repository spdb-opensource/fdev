package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.fdevenvconfig.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

@Document(collection = "defined_variables")
public class DefinedVariables implements Serializable {
    @Id
    @JsonIgnore
    private ObjectId _id;
    //自定义部署标识
    @Field(Dict.ID)
    private String id;
    //应用ID
    @Field(Dict.APP_ID)
    private String appId;
    //配置文件更新标识
    @Field(Dict.CONFIG_UPDATE_FLAG)
    private String configUpdateFlag;
    //重新部署标识
    @Field(Dict.RE_DEPLOY_FLAG)
    private String reDeployFlag;
    //分支名
    @Field(Dict.REF)
    private String ref;
    //自定义部署参数
    @Field(Dict.VARIABLES)
    private Map variables;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Map getVariables() {
        return variables;
    }

    public void setVariables(Map variables) {
        this.variables = variables;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfigUpdateFlag() {
        return configUpdateFlag;
    }

    public void setConfigUpdateFlag(String configUpdateFlag) {
        this.configUpdateFlag = configUpdateFlag;
    }

    public String getReDeployFlag() {
        return reDeployFlag;
    }

    public void setReDeployFlag(String reDeployFlag) {
        this.reDeployFlag = reDeployFlag;
    }
}
