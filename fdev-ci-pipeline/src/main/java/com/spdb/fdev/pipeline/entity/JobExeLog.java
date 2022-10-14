package com.spdb.fdev.pipeline.entity;

import com.spdb.fdev.base.annotation.nonull.AutoInc;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
public class JobExeLog {
    @Field("job_exe_id")
    @Indexed(unique = true)
    public String jobExeId;

    @Field("job_number")
    @Indexed(unique = true)
    public Long jobNumber = 0L;

    @Field("status")
    private String status;

    @Field("pipeline_exe_id")
    private String pipelineExeId;

    @Field("pipeline_id")
    private String pipelineId;

    @Field("context")
    private String context;

    public String getJobExeId() {
        return jobExeId;
    }

    public void setJobExeId(String jobExeId) {
        this.jobExeId = jobExeId;
    }

    public Long getJobNumber() {
        return jobNumber;
    }

    public void setJobNumber(Long jobNumber) {
        this.jobNumber = jobNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPipelineExeId() {
        return pipelineExeId;
    }

    public void setPipelineExeId(String pipelineExeId) {
        this.pipelineExeId = pipelineExeId;
    }

    public String getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(String pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
