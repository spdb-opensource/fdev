package com.spdb.fdev.pipeline.entity;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class Stage {
    @Field("name")
    private String name;

    @Field("jobs")
    private List<Job> jobs;

    @Field("status")
    private String status;
    //流水线模板传入的构造

    public Stage(String name, List<Job> jobs) {
        this.name = name;
        this.jobs = jobs;
    }

    public Stage() {
    }

    @Transient
    private String stageName;       //模版中返回stage名
    @Transient
    private List<String> jobNames;   //模版中返回每个stage下面的job名称

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public List<String> getJobNames() {
        return jobNames;
    }

    public void setJobNames(List<String> jobNames) {
        this.jobNames = jobNames;
    }

    @Override
    public String toString() {
        return "Stage{" +
                "name='" + name + '\'' +
                ", jobs=" + jobs +
                ", status='" + status + '\'' +
                ", stageName='" + stageName + '\'' +
                ", jobNames=" + jobNames +
                '}';
    }
}
