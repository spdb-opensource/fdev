package com.spdb.fdev.pipeline.entity;

import com.spdb.fdev.base.annotation.nonull.AutoInc;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Document(collection = "job_exe")
public class JobExe implements Serializable {

    private static final long serialVersionUID = -6587397506401212956L;

    @Field("exeId")
    @Indexed(unique = true)
    public String exeId;

    @Field("jobNumber")
    @AutoInc
    @Indexed(unique = true)
    public Long jobNumber = 0L;

    @Field("token")
    private String token;

   /* @Field("executorId")
    private String executorId;*/

    /*@Field("context")
    private List<String> context;*/

    @Field("jobStartTime")
    private String jobStartTime;

    @Field("jobEndTime")
    private String jobEndTime;

    @Field("jobCostTime")
    private String jobCostTime;

    @Field("status")
    private String status;

  /*  @Field("plugins")
    private List<JobExePlugin> plugins;*/

  /*  @Field("platform")
    private String platform;*/

    @Field("info")
    private Map<String, Object> info;

    @Field("pipelineExeId")
    private String pipelineExeId;

    @Field("stageName")
    private String stageName;

    @Field("stageIndex")
    private Integer stageIndex;

    @Field("jobIndex")
    private Integer jobIndex;

    @Field("jobName")
    private String jobName;

    @Field("image")
    private Map<String, String> image;

    @Field("pipelineId")
    private String pipelineId;

    @Field("user")
    private Author user;

    @Field("minioLogUrl")
    private String minioLogUrl;

    @Field("steps")
    private List<Map> steps;

    public String getExeId() {
        return exeId;
    }

    public void setExeId(String exeId) {
        this.exeId = exeId;
    }

    public Long getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(Long jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

   /* public String getExecutorId() {
        return executorId;
    }

    public void setExecutorId(String executorId) {
        this.executorId = executorId;
    }*/

 /*   public List<String> getContext() {
        return context;
    }

    public void setContext(List<String> context) {
        this.context = context;
    }
*/
    public String getJobStartTime() {
        return jobStartTime;
    }

    public void setJobStartTime(String jobStartTime) {
        this.jobStartTime = jobStartTime;
    }

    public String getJobEndTime() {
        return jobEndTime;
    }

    public void setJobEndTime(String jobEndTime) {
        this.jobEndTime = jobEndTime;
    }

    public String getJobCostTime() {
        return jobCostTime;
    }

    public void setJobCostTime(String jobCostTime) {
        this.jobCostTime = jobCostTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

  /*  public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }*/

    public String getPipelineExeId() {
        return pipelineExeId;
    }

    public void setPipelineExeId(String pipelineExeId) {
        this.pipelineExeId = pipelineExeId;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public Integer getStageIndex() {
        return stageIndex;
    }

    public void setStageIndex(Integer stageIndex) {
        this.stageIndex = stageIndex;
    }

    public Integer getJobIndex() {
        return jobIndex;
    }

    public void setJobIndex(Integer jobIndex) {
        this.jobIndex = jobIndex;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Map<String, String> getImage() {
        return image;
    }

    public void setImage(Map<String, String> image) {
        this.image = image;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getMinioLogUrl() {
        return minioLogUrl;
    }

    public void setMinioLogUrl(String minioLogUrl) {
        this.minioLogUrl = minioLogUrl;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Author getUser() {
        return user;
    }

    public void setUser(Author user) {
        this.user = user;
    }


    public List<Map> getSteps() {
        return steps;
    }

    public void setSteps(List<Map> steps) {
        this.steps = steps;
    }

    public Map getInfo() {
        return info;
    }

    public void setInfo(Map info) {
        this.info = info;
    }
}
