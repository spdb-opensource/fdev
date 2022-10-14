package com.spdb.fdev.pipeline.entity;

import com.spdb.fdev.base.dict.Dict;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @Author: c-jiangjk
 * @Date: 2021/4/2 14:19
 */
@Document(collection = "runnerInfo")
public class RunnerInfo implements Serializable {

    @Field("runnerId")
//    @Indexed(unique = true)
    private String runnerId;

    @Field(Dict.NAME)
    private String name;

    @Field(Dict.VERSION)
    private String version;

    @Field(Dict.REVISION)
    private String revision;

    @Field(Dict.PLATFORM)
    private String platform;

    @Field(Dict.ARCHITECTURE)
    private String architecture;

    @Field(Dict.EXECUTOR)
    private String executor;

    @Field("token")
//    @Indexed(unique = true)
    private String token;

    @Field("status")
    private String status;              //0废弃，使用

    public String getRunnerId() {
        return runnerId;
    }

    public void setRunnerId(String runnerId) {
        this.runnerId = runnerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getExecutor() {
        return executor;
    }

    public void setExecutor(String executor) {
        this.executor = executor;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
