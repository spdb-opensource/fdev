package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @program:fdev-process
 * @author: xxx
 * @createTime: 2021-01-22 16:55
 */
@ApiModel(value="输出")
public class OutputFieldList {

    @ApiModelProperty(value ="输出域模型ID")
    private String fdev_process_fieldId;

    public String getFdev_process_fieldId() {
        return fdev_process_fieldId;
    }

    public void setFdev_process_fieldId(String fdev_process_fieldId) {
        this.fdev_process_fieldId = fdev_process_fieldId;
    }

    public OutputFieldList() {

    }
}
