package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @program:fdev-process
 * @author: xxx
 * @createTime: 2021-01-22 16:55
 */
@ApiModel(value="可选值相关")
public class FeComponentList {

    @ApiModelProperty(value ="前端组件类型")
    private String feType;
    //0-button 1:page

    @ApiModelProperty(value ="组件路径")
    private String path;

    @ApiModelProperty(value ="组件用途")
    private String purpose;
    //0-管理设置 1-功能页面

    public FeComponentList() {

    }

    public String getFeType() {
        return feType;
    }

    public void setFeType(String feType) {
        this.feType = feType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
