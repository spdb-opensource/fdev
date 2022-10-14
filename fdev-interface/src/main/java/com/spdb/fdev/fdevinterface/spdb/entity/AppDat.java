package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

/**
 * 单个应用生成的加密介质
 *
 * @author xxx
 * @date 2020/7/30 15:36
 */
@Document(collection = Dict.INTERFACE_APP_DAT)
public class AppDat implements Serializable {

    private static final long serialVersionUID = -5193100991291911291L;

    @Id
    private String id;

    // 所属项目名称（应用英文名）
    @Field(Dict.PROJECT_NAME)
    private String projectName;

    private String appId;

    // 分支
    @Field(Dict.BRANCH)
    private String branch;

    // 场景数
    @Field(Dict.ROUTE_NUM)
    private Integer routeNum;

    // project.json文件内容
    @Field(Dict.PROJECT_JSON)
    private Map<String, Object> projectJson;

    // repo_mspmk-cli-XXX.json文件内容
    @Field(Dict.REPO_JSON)
    private Map<String, Object> repoJson;

    // repo_mspmk-cli-XXX.json文件的MD5
    @Field(Dict.REPO_JSON_MD5)
    private String repoJsonMD5;

    // project.json里routes的版本号
    @Field(Dict.ROUTES_VERSION)
    private Integer routesVersion;

    // CICD时间
    @Field(Dict.CICD_TIME)
    private String cicdTime;

    // 仓库配置介质tar包名（包括生成时间）
    @Field(Dict.REPO_TAR_NAME)
    private String repoTarName;

    // 下载链接
    @Field(Dict.REPO_TAR_URL)
    private String repoTarUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getRouteNum() {
        return routeNum;
    }

    public void setRouteNum(Integer routeNum) {
        this.routeNum = routeNum;
    }

    public Map<String, Object> getProjectJson() {
        return projectJson;
    }

    public void setProjectJson(Map<String, Object> projectJson) {
        this.projectJson = projectJson;
    }

    public Map<String, Object> getRepoJson() {
        return repoJson;
    }

    public void setRepoJson(Map<String, Object> repoJson) {
        this.repoJson = repoJson;
    }

    public String getRepoJsonMD5() {
        return repoJsonMD5;
    }

    public void setRepoJsonMD5(String repoJsonMD5) {
        this.repoJsonMD5 = repoJsonMD5;
    }

    public Integer getRoutesVersion() {
        return routesVersion;
    }

    public void setRoutesVersion(Integer routesVersion) {
        this.routesVersion = routesVersion;
    }

    public String getCicdTime() {
        return cicdTime;
    }

    public void setCicdTime(String cicdTime) {
        this.cicdTime = cicdTime;
    }

    public String getRepoTarName() {
        return repoTarName;
    }

    public void setRepoTarName(String repoTarName) {
        this.repoTarName = repoTarName;
    }

    public String getRepoTarUrl() {
        return repoTarUrl;
    }

    public void setRepoTarUrl(String repoTarUrl) {
        this.repoTarUrl = repoTarUrl;
    }

}
