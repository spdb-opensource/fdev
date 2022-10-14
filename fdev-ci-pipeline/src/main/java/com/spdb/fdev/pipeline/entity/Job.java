package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class Job {

    @Field("name")
    private String name;

    @Field("desc")
    private String desc;

    @Field("info")
    private JobInfo info;

    @Field("steps")
    private List<Step> steps;

    @Field("platform")
    private String platform;

    @Field("warning")
    private Boolean warning;    //true有必填参数未输入；false代表没有必填参数未输入

    @Field("image")
    private Images image;     //镜像环境

    @Field("runnerClusterId")
    private String runnerClusterId;         //构建集群id

    public Job() {
    }

    public Job(String name, String desc, JobInfo info, List<Step> steps, String platform, Boolean warning, Images image) {
        this.name = name;
        this.desc = desc;
        this.info = info;
        this.steps = steps;
        this.platform = platform;
        this.warning = warning;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public JobInfo getInfo() {
        return info;
    }

    public void setInfo(JobInfo info) {
        this.info = info;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public Boolean getWarning() {
        return warning;
    }

    public void setWarning(Boolean warning) {
        this.warning = warning;
    }

    public Images getImage() {
        return image;
    }

    public void setImage(Images image) {
        this.image = image;
    }

    public String getRunnerClusterId() {
        return runnerClusterId;
    }

    public void setRunnerClusterId(String runnerClusterId) {
        this.runnerClusterId = runnerClusterId;
    }

    @Override
    public String toString() {
        return "Job{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", info=" + info +
                ", steps=" + steps +
                ", platform='" + platform + '\'' +
                ", warning=" + warning +
                ", image=" + image +
                ", runnerClusterId='" + runnerClusterId + '\'' +
                '}';
    }
}
