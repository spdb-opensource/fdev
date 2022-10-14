package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document("pipelineCronBatch")
public class PipelineCronBatch {

    @Field("cronId")
    private String cronId;

    @Field("batchNo")
    private String batchNo;             //批次号

    @Field("batchStatus")
    private String batchStatus;         //当前批次的执行状态

    @Field("count")
    private int count;                  //当前批次所执行的流水线

    public PipelineCronBatch() {
    }

    public PipelineCronBatch(String cronId, String batchNo, String batchStatus) {
        this.cronId = cronId;
        this.batchNo = batchNo;
        this.batchStatus = batchStatus;
    }

    public String getCronId() {
        return cronId;
    }

    public void setCronId(String cronId) {
        this.cronId = cronId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getBatchStatus() {
        return batchStatus;
    }

    public void setBatchStatus(String batchStatus) {
        this.batchStatus = batchStatus;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
