package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author dafeng
 * @date 2021/2/23 16:17
 */
@Component
@ApiModel(value = "自动触发时选择push")
public class Push {

    @Field("switchFlag")
    @ApiModelProperty(value = "触发的开关")
    private Boolean switchFlag;

    @Field("pushParams")
    @ApiModelProperty(value = "触发的参数")
    private List<Map> pushParams;

    public Boolean getSwitchFlag() {
        return switchFlag;
    }

    public void setSwitchFlag(Boolean switchFlag) {
        this.switchFlag = switchFlag;
    }

    public List<Map> getPushParams() {
        return pushParams;
    }

    public void setPushParams(List<Map> pushParams) {
        this.pushParams = pushParams;
    }

    @Override
    public String toString() {
        return "Push{" +
                "switchFlag=" + switchFlag +
                ", pushParams=" + pushParams +
                '}';
    }
}
