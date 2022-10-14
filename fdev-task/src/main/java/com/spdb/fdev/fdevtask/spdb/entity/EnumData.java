package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @program:fdev-process
 * @author: xxx
 * @createTime: 2021-01-22 16:55
 */
@ApiModel(value="可选值相关")
public class EnumData {

    @ApiModelProperty(value ="枚举值")
    private String value;

    @ApiModelProperty(value ="枚举值描述")
    private String dic;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDic() {
        return dic;
    }

    public void setDic(String dic) {
        this.dic = dic;
    }
}
