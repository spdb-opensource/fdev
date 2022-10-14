package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = Dict.INTERFACE_ROUTES_RELATION)
public class RoutesRelation implements Serializable {

    private static final long serialVersionUID = 1263736618855598265L;

    @Id
    private String id;

    @Field(Dict.NAME)
    private String name;

    //调用项目id
    @Field(Dict.SOURCE_PROJECT)
    private String sourceProject;

    //路由提供项目id
    @Field(Dict.TARGET_PROJECT)
    private String targetProject;

    // 分支
    @Field(Dict.BRANCH)
    private String branch;

    // project.json文件的md5值
    @Field(Dict.MD5_STR)
    private String md5Str;

    // project.json文件里deps字段下routes字段的md5值
    @Field(Dict.ROUTES_MD5)
    private String routesMD5;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceProject() {
        return sourceProject;
    }

    public void setSourceProject(String sourceProject) {
        this.sourceProject = sourceProject;
    }

    public String getTargetProject() {
        return targetProject;
    }

    public void setTargetProject(String targetProject) {
        this.targetProject = targetProject;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5Str() {
        return md5Str;
    }

    public void setMd5Str(String md5Str) {
        this.md5Str = md5Str;
    }

    public String getRoutesMD5() {
        return routesMD5;
    }

    public void setRoutesMD5(String routesMD5) {
        this.routesMD5 = routesMD5;
    }
}
