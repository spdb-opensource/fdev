package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
public class ConditionValue {

    @Field("stage_name")
    private String stageName;           //job前置的stage name

    @Field("job_name")
    private String jobName;             //job前置的job name

    public ConditionValue() {
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String stageName) {
        this.stageName = stageName;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }
}
