package com.spdb.fdev.fdevinterface.spdb.vo;

import java.util.List;

/**
 * 路由列表显示层对象
 */
public class RoutesShow {

    private String id;

    private String appId;

    // 路由名称
    private String name;

    // 项目id
    private String projectName;

    // 分支
    private String branch;

    // 路由调用路径
    private String path;

    // 路由约束
    private Object query;

    //类型
    private String module;

    //版本
    private Integer ver;

    //是否为最新
    private String isNew;

    //路由访问权限要求
    private List<String> authCheck;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Object getQuery() {
        return query;
    }

    public void setQuery(Object query) {
        this.query = query;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public List<String> getAuthCheck() {
        return authCheck;
    }

    public void setAuthCheck(List<String> authCheck) {
        this.authCheck = authCheck;
    }

    public Integer getVer() {
        return ver;
    }

    public void setVer(Integer ver) {
        this.ver = ver;
    }

    public String getIsNew() {
        return isNew;
    }

    public void setIsNew(String isNew) {
        this.isNew = isNew;
    }
}
