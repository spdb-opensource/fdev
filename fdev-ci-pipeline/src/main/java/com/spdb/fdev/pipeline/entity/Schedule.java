package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author dafeng
 * @date 2021/2/23 16:47
 */
@Component
@ApiModel(value = "定时方式")
public class Schedule {

    @Field("switchFlag")
    @ApiModelProperty(value = "触发的开关")
    private Boolean switchFlag;

    @Field("scheduleParams")
    @ApiModelProperty(value = "定时触发的参数")
    private List<Map> scheduleParams;

    public Boolean getSwitchFlag() {
        return switchFlag;
    }

    public void setSwitchFlag(Boolean switchFlag) {
        this.switchFlag = switchFlag;
    }

    public List<Map> getScheduleParams() {
        return scheduleParams;
    }

    public void setScheduleParams(List<Map> scheduleParams) {
        this.scheduleParams = scheduleParams;
    }

    @Override
    public String toString() {
        return "Schedule{" +
                "switchFlag=" + switchFlag +
                ", scheduleParams=" + scheduleParams +
                '}';
    }
}
