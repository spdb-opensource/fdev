package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = Dict.INTERFACE_ROUTES_API)
public class RoutesApi implements Serializable {

    private static final long serialVersionUID = 4132341272979554426L;

    @Id
    private String id;

    // 场景Id
    @Field(Dict.NAME)
    private String name;

    // 场景加载容器
    @Field(Dict.MODULE)
    private String module;

    // 项目id
    @Field(Dict.PROJECT_NAME)
    private String projectName;

    // 分支
    @Field(Dict.BRANCH)
    private String branch;

    // 路由调用路径
    @Field(Dict.PATH)
    private String path;

    // 自定义的场景参数 (原生初始化用)
    @Field(Dict.PARAMS)
    private Object params;

    // 路由跳转传参
    @Field(Dict.QUERY)
    private Object query;

    // 提供给原生的额外信息
    @Field(Dict.EXTRA)
    private List<Object> extra;

    // 路由访问权限要求
    @Field(Dict.AUTH_CHECK)
    private List<String> authCheck;

    // project.json文件的md5值
    @Field(Dict.MD5_STR)
    private String md5Str;

    // project.json文件里routes字段的md5值
    @Field(Dict.ROUTES_MD5)
    private String routesMD5;

    // 路由版本号
    @Field(Dict.VER)
    private Integer ver;

    // 是否为最新版本，0：否，1：是
    @Field(Dict.IS_NEW)
    private Integer isNew;

    // 标识扫描方式：自动/手动
    @Field(Dict.TYPE)
    private String type;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

    // 更新时间
    @Field(Dict.UPDATE_TIME)
    private String updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Object getQuery() {
        return query;
    }

    public void setQuery(Object query) {
        this.query = query;
    }

    public List<Object> getExtra() {
        return extra;
    }

    public void setExtra(List<Object> extra) {
        this.extra = extra;
    }

    public List<String> getAuthCheck() {
        return authCheck;
    }

    public void setAuthCheck(List<String> authCheck) {
        this.authCheck = authCheck;
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

    public Integer getVer() {
        return ver;
    }

    public void setVer(Integer ver) {
        this.ver = ver;
    }

    public Integer getIsNew() {
        return isNew;
    }

    public void setIsNew(Integer isNew) {
        this.isNew = isNew;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
