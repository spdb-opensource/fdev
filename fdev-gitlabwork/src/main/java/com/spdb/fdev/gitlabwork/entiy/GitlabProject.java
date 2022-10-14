package com.spdb.fdev.gitlabwork.entiy;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * GitlabProject
 *
 * @blame Android Team
 */
@Document(collection = "gitlabProjectInfo")
public class GitlabProject implements Serializable {
    @Field("project_id")
    @ApiModelProperty(value="项目id")
    private String project_id;

    @Field("name_en")
    @ApiModelProperty(value="项目英文名")
    private String nameEn;

    @Field("name_ch")
    @ApiModelProperty(value="应用中文名")
    private String nameCh;

    @Field("group")
    @ApiModelProperty(value="小组")
    private String group;

    @Field("git")
    @ApiModelProperty(value="git地址")
    private String git;

    @Field("web_url")
    @ApiModelProperty(value="git地址")
    private String web_url;

    @Field("sign")
    @ApiModelProperty(value="标识位  0 新增 1未同步 2 已同步")
    private int sign;

    public String getWeb_url() {
        return web_url;
    }

    public void setWeb_url(String web_url) {
        this.web_url = web_url;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameCh() {
        return nameCh;
    }

    public void setNameCh(String nameCh) {
        this.nameCh = nameCh;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getGit() {
        return git;
    }

    public void setGit(String git) {
        this.git = git;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }
}
