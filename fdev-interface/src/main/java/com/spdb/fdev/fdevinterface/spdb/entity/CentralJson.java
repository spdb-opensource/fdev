package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

/**
 * @author xxx
 * @date 2020/7/28 19:27
 */
@Document(collection = Dict.INTERFACE_CENTRAL_JSON)
public class CentralJson implements Serializable {

    private static final long serialVersionUID = -100433977811424903L;

    @Id
    private String id;

    // 环境
    @Field(Dict.ENV)
    private String env;

    // central.json文件内容
    @Field(Dict.CENTRAL_JSON)
    private Map<String, Object> centralJson;

    // repo.json文件模板
    @Field(Dict.REPO_JSON)
    private Map<String, Object> repoJson;

    @Field(Dict.UPDATE_TIME)
    private String updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public Map<String, Object> getCentralJson() {
        return centralJson;
    }

    public void setCentralJson(Map<String, Object> centralJson) {
        this.centralJson = centralJson;
    }

    public Map<String, Object> getRepoJson() {
        return repoJson;
    }

    public void setRepoJson(Map<String, Object> repoJson) {
        this.repoJson = repoJson;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
