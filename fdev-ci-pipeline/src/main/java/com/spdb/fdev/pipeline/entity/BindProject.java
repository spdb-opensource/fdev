package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class BindProject {

    @ApiModelProperty("应用英文名")
    @Field("nameEn")
    private String nameEn;

    @ApiModelProperty("应用中文名")
    @Field("nameCn")
    private String nameCn;

    @ApiModelProperty("应用ID")
    @Field("projectId")
    private String projectId;

    @Transient
    private List<String> branch;     //这里分支不再入库

    @ApiModelProperty("应用GITLAB id")
    @Field("gitlabProjectId")
    private Integer gitlabProjectId;

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<String> getBranch() {
        return branch;
    }

    public void setBranch(List<String> branch) {
        this.branch = branch;
    }

    public Integer getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(Integer gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }

    @Override
    public String toString() {
        return "BindProject{" +
                "nameEn='" + nameEn + '\'' +
                ", nameCn='" + nameCn + '\'' +
                ", projectId='" + projectId + '\'' +
                ", branch=" + branch +
                ", gitlabProjectId=" + gitlabProjectId +
                '}';
    }
}
