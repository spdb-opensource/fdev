package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @program: fdev-process
 * @description:
 * @author: c-gaoys
 * @create: 2021-01-29 15:47
 **/
@ApiModel(value="组件参数设置")
public class ActivityParams {

    @ApiModelProperty("组件id")
    private String activityId;

    @ApiModelProperty("组件name")
    private String activityName;

    @ApiModelProperty("参数设置")
    private List<Map<String,String>> params;

    public ActivityParams() {
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public List<Map<String,String>> getParams() {
        return params;
    }

    public void setParams(List<Map<String,String>> params) {
        this.params = params;
    }
}
