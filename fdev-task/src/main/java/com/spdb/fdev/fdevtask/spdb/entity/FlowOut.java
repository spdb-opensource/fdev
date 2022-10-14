package com.spdb.fdev.fdevtask.spdb.entity;

import com.spdb.fdev.fdevtask.spdb.entity.ActivityParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;
import java.util.Map;

/**
 * @program: fdev-process
 * @description: 目标flow
 * @author: c-gaoys
 * @create: 2021-01-22 17:10
 **/
@ApiModel(value="状态转出信息")
public class FlowOut {

    @ApiModelProperty("状态id")
    private String fdevProcessStatusId;

    @ApiModelProperty("准出条件")
    private List<String> checkFieldList;

    @ApiModelProperty("0-手动触发，1-自动触发")
    private int type;

    @ApiModelProperty("功能组件{fdevProcessActivityId : 组件id,paramsJson:输入参数Json串}")
    private List<ActivityParams> activityList;

    @ApiModelProperty("允许修改的人员权限，当手动触发时需要设置,{id:角色id,name:角色名称}")
    private List<Map<String,String>> permissionOwnerList;

    public FlowOut() {
    }

    public FlowOut(String fdevProcessStatusId) {
        this.fdevProcessStatusId = fdevProcessStatusId;
    }

    public String getFdevProcessStatusId() {
        return fdevProcessStatusId;
    }

    public void setFdevProcessStatusId(String fdevProcessStatusId) {
        this.fdevProcessStatusId = fdevProcessStatusId;
    }

    public List<String> getCheckFieldList() {
        return checkFieldList;
    }

    public void setCheckFieldList(List<String> checkFieldList) {
        this.checkFieldList = checkFieldList;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<ActivityParams> getActivityList() {
        return activityList;
    }

    public void setActivityList(List<ActivityParams> activityList) {
        this.activityList = activityList;
    }

    public List<Map<String, String>> getPermissionOwnerList() {
        return permissionOwnerList;
    }

    public void setPermissionOwnerList(List<Map<String, String>> permissionOwnerList) {
        this.permissionOwnerList = permissionOwnerList;
    }
}
