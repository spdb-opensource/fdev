package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Objects;

@ApiModel(value="无代码变更的信息")
public class NoCodeInfo {
    /**
     * 数据库变更需要的东西
     * @ApiModelProperty(value = "任务id")
     * private String taskId;
     * */
    @ApiModelProperty(value="任务详情")
    private String taskDesc;
    @ApiModelProperty(value="截止时间")
    private String finishTime;
    @ApiModelProperty(value="相关状态")
    private Boolean nextStageStatus;
    @ApiModelProperty(value = "相关人员")
    private Relator[] relators;

    public String getTaskDesc() {
        return taskDesc;
    }

    public void setTaskDesc(String taskDesc) {
        this.taskDesc = taskDesc;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public Relator[] getRelators() {
        return relators;
    }

    public void setRelators(Relator[] relators) {
        this.relators = relators;
    }

    public Boolean getNextStageStatus() {
        return nextStageStatus;
    }

    public void setNextStageStatus(Boolean nextStageStatus) {
        this.nextStageStatus = nextStageStatus;
    }
}
