package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @program:fdev-process
 * @author: xxx
 * @createTime: 2021-01-22 16:55
 */
@ApiModel(value="输入")
public class InputFieldList {

    @ApiModelProperty(value ="输入域模型ID")
    private String fdev_process_fieldId;

    public String getFdev_process_fieldId() {
        return fdev_process_fieldId;
    }

    public void setFdev_process_fieldId(String fdev_process_fieldId) {
        this.fdev_process_fieldId = fdev_process_fieldId;
    }

    public InputFieldList() {

    }
}
