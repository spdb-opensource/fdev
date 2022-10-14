package com.spdb.fdev.pipeline.entity;

import com.spdb.fdev.base.annotation.nonull.AutoInc;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Document(collection = "pipeline_exe")
public class PipelineExe {

    @Field("exeId")
    @Indexed(unique = true)
    public String exeId;

    @Field("pipelineNumber")
    @Indexed(unique = true)
    @AutoInc
    public Long pipelineNumber = 0L;

    @Field("pipelineId")
    private String pipelineId;

    @Field("pipelineNameId")
    private String pipelineNameId;

    @Field("pipelineName")
    private String pipelineName;

    @Field("bindProject")
    private Map<String, Object> bindProject;

    @Field("project_info")
    private Map<String, String> projectInfo;

    @Field("git_info")
    private Map<String, String> gitInfo;

    @Field("stages")
    private List<Map<String, Object>> stages;

    @Field("update_time")
    private String updateTime;

    @Field("startTime")
    private String startTime;

    @Field("endTime")
    private String endTime;

    @Field("costTime")
    private String costTime;

    @Field("status")
    private String status;

    @Field("variables")
    private List<Map> variables;

    @Field("volumes")
    private List<Map> volumes;

    @Field("branch")
    private String branch;

    @Field("triggerMode")
    private  String triggerMode;//manual 手动  auto 自动

    @Field("user")
    private Author user;

    @Field("artifacts")
    private List<Map> artifacts;

    @Transient
    @ApiModelProperty("是否可以重试0：否1：是")
    private String retry;

    @Field("commitTitle")
    @ApiModelProperty("push触发保存提交记录")
    private String commitTitle;

    @Field("commitId")
    @ApiModelProperty("push触发保存提交记录Id")
    private String commitId;

    public String getExeId() {
        return exeId;
    }

    public void setExeId(String exeId) {
        this.exeId = exeId;
    }

    public Long getPipelineNumber() {
        return pipelineNumber;
    }

    public void setPipelineNumber(Long pipelineNumber) {
        this.pipelineNumber = pipelineNumber;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getPipelineNameId() {
        return pipelineNameId;
    }

    public void setPipelineNameId(String pipelineNameId) {
        this.pipelineNameId = pipelineNameId;
    }

    public Map getBindProject() {
        return bindProject;
    }

    public Map<String, String> getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(Map<String, String> projectInfo) {
        this.projectInfo = projectInfo;
    }

    public Map<String, String> getGitInfo() {
        return gitInfo;
    }

    public void setGitInfo(Map<String, String> gitInfo) {
        this.gitInfo = gitInfo;
    }

    public void setBindProject(Map<String, Object> bindProject) {
        this.bindProject = bindProject;
    }

    public List<Map<String, Object>> getStages() {
        return stages;
    }

    public void setStages(List<Map<String, Object>> stages) {
        this.stages = stages;
    }


    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getCostTime() {
        return costTime;
    }

    public void setCostTime(String costTime) {
        this.costTime = costTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Map> getVariables() {
        return variables;
    }

    public void setVariables(List<Map> variables) {
        this.variables = variables;
    }

    public List<Map> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<Map> volumes) {
        this.volumes = volumes;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getTriggerMode() {
        return triggerMode;
    }

    public void setTriggerMode(String triggerMode) {
        this.triggerMode = triggerMode;
    }

    public String getPipelineName() {
        return pipelineName;
    }

    public void setPipelineName(String pipelineName) {
        this.pipelineName = pipelineName;
    }

    public List<Map> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(List<Map> artifacts) {
        this.artifacts = artifacts;
    }

    public String getRetry() {
        return retry;
    }

    public void setRetry(String retry) {
        this.retry = retry;
    }

    public Author getUser() {
        return user;
    }

    public void setUser(Author user) {
        this.user = user;
    }

    public String getCommitTitle() {
        return commitTitle;
    }

    public void setCommitTitle(String commitTitle) {
        this.commitTitle = commitTitle;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }
}
