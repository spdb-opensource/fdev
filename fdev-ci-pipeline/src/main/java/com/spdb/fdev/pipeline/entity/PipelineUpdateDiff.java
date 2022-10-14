package com.spdb.fdev.pipeline.entity;


import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 记录每次pipeline更新产生的变更信息
 *
 *
 */
@Document("pipelineUpdateDiff")
public class PipelineUpdateDiff implements Serializable {

    @Field("id")
    private String id;

    @Field("sourcePipelineId")
    private String sourcePipelineId;            //源pipeline

    @Field("targetPipelineId")
    private String targetPipelineId;            //目标pipeline

    @Field("pipelineNameId")
    private String pipelineNameId;               //pipeline的nameId

    @Field("diff")
    private List<Map> diff;                     //不同的内容

    public PipelineUpdateDiff() {
    }

    public PipelineUpdateDiff(String id, String sourcePipelineId, String targetPipelineId, String pipelineNameId, List<Map> diff) {
        this.id = id;
        this.sourcePipelineId = sourcePipelineId;
        this.targetPipelineId = targetPipelineId;
        this.pipelineNameId = pipelineNameId;
        this.diff = diff;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourcePipelineId() {
        return sourcePipelineId;
    }

    public void setSourcePipelineId(String sourcePipelineId) {
        this.sourcePipelineId = sourcePipelineId;
    }

    public String getTargetPipelineId() {
        return targetPipelineId;
    }

    public void setTargetPipelineId(String targetPipelineId) {
        this.targetPipelineId = targetPipelineId;
    }

    public String getPipelineNameId() {
        return pipelineNameId;
    }

    public void setPipelineNameId(String pipelineNameId) {
        this.pipelineNameId = pipelineNameId;
    }

    public List<Map> getDiff() {
        return diff;
    }

    public void setDiff(List<Map> diff) {
        this.diff = diff;
    }

    @Override
    public String toString() {
        return "PipelineUpdateDiff{" +
                "id='" + id + '\'' +
                ", sourcePipelineId='" + sourcePipelineId + '\'' +
                ", targetPipelineId='" + targetPipelineId + '\'' +
                ", pipelineNameId='" + pipelineNameId + '\'' +
                ", diff=" + diff +
                '}';
    }
}
