package com.spdb.fdev.fdemand.spdb.dto;

import io.swagger.annotations.ApiModelProperty;

import java.util.List;

public class DemandAssessDto extends PageDto {

    @ApiModelProperty(value = "需求名称/编号")
    private String oaContactNameNo;

    @ApiModelProperty(value = "需求牵头小组集合")
    private List<String> demandLeaderGroups;

    @ApiModelProperty(value = "需求牵头人")
    private List<String> demandLeader;

    @ApiModelProperty(value = "优先级")
    private String priority;

    @ApiModelProperty(value = "超期分类")
    private String overdueType;

    @ApiModelProperty(value = "评估天数(>=?)")
    private Integer gteAssessDays;

    @ApiModelProperty(value = "需求状态，分析中:1，分析完成:2，撤销:9")
    private Integer demandStatus;

    @ApiModelProperty(value = "需求文档状态")
    private String confState;

    @ApiModelProperty(value = "需求进行中天数")
    private Integer goingDays;

    @ApiModelProperty(value = "需求定稿后天数")
    private Integer finalDays;

    public String getOaContactNameNo() {
        return oaContactNameNo;
    }

    public void setOaContactNameNo(String oaContactNameNo) {
        this.oaContactNameNo = oaContactNameNo;
    }

    public List<String> getDemandLeaderGroups() {
        return demandLeaderGroups;
    }

    public void setDemandLeaderGroups(List<String> demandLeaderGroups) {
        this.demandLeaderGroups = demandLeaderGroups;
    }

    public List<String> getDemandLeader() {
        return demandLeader;
    }

    public void setDemandLeader(List<String> demandLeader) {
        this.demandLeader = demandLeader;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getOverdueType() {
        return overdueType;
    }

    public void setOverdueType(String overdueType) {
        this.overdueType = overdueType;
    }

    public Integer getGteAssessDays() {
        return gteAssessDays;
    }

    public void setGteAssessDays(Integer gteAssessDays) {
        this.gteAssessDays = gteAssessDays;
    }

    public Integer getDemandStatus() {
        return demandStatus;
    }

    public void setDemandStatus(Integer demandStatus) {
        this.demandStatus = demandStatus;
    }

    public String getConfState() {
        return confState;
    }

    public void setConfState(String confState) {
        this.confState = confState;
    }

    public Integer getGoingDays() {
        return goingDays;
    }

    public void setGoingDays(Integer goingDays) {
        this.goingDays = goingDays;
    }

    public Integer getFinalDays() {
        return finalDays;
    }

    public void setFinalDays(Integer finalDays) {
        this.finalDays = finalDays;
    }
}
