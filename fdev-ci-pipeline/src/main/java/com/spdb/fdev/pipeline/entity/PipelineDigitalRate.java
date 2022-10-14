package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * *pipeline的数据化展示
 * <p>
 * exeTotal 流水线构建总数，successExeTotal 成功构建总数，errorExeTotal 失败构建总数，successRate 成功率，aveErrorTime 失败平均修复时长
 **/
@Document(collection = "pipelineDigitalRate")
public class PipelineDigitalRate implements Serializable {

    @Field("id")
    private String id;

    //pipeline的nameId 用来标识pipeline
    @Field("nameId")
    private String nameId;

    @Field("exeTotal")
    private Double exeTotal;        //流水线构建总数

    @Field("successExeTotal")
    private Double successExeTotal; //成功构建总数

    @Field("errorExeTotal")
    private Double errorExeTotal;   //失败构建总数

    @Field("successRate")
    private String successRate;     // 成功率

    @Field("aveErrorTime")
    private Double aveErrorTime;    //失败平均修复时长

    public PipelineDigitalRate(String id, String nameId, Double exeTotal, Double successExeTotal, Double errorExeTotal, String successRate, Double aveErrorTime) {
        this.id = id;
        this.nameId = nameId;
        this.exeTotal = exeTotal;
        this.successExeTotal = successExeTotal;
        this.errorExeTotal = errorExeTotal;
        this.successRate = successRate;
        this.aveErrorTime = aveErrorTime;
    }

    public PipelineDigitalRate() {
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameId() {
        return nameId;
    }

    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public Double getExeTotal() {
        return exeTotal;
    }

    public void setExeTotal(Double exeTotal) {
        this.exeTotal = exeTotal;
    }

    public Double getSuccessExeTotal() {
        return successExeTotal;
    }

    public void setSuccessExeTotal(Double successExeTotal) {
        this.successExeTotal = successExeTotal;
    }

    public Double getErrorExeTotal() {
        return errorExeTotal;
    }

    public void setErrorExeTotal(Double errorExeTotal) {
        this.errorExeTotal = errorExeTotal;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public Double getAveErrorTime() {
        return aveErrorTime;
    }

    public void setAveErrorTime(Double aveErrorTime) {
        this.aveErrorTime = aveErrorTime;
    }

    @Override
    public String toString() {
        return "PipelineDigitalRate{" +
                "id='" + id + '\'' +
                ", nameId='" + nameId + '\'' +
                ", exeTotal=" + exeTotal +
                ", successExeTotal=" + successExeTotal +
                ", errorExeTotal=" + errorExeTotal +
                ", successRate=" + successRate +
                ", aveErrorTime=" + aveErrorTime +
                '}';
    }
}
