package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author dafeng
 * @date 2021/2/23 16:53
 */
@Component
@ApiModel(value = "触发规则设置")
public class TriggerRules {

    @Field("push")
    @ApiModelProperty(value = "push方式")
    private Push push;

    @Field("schedule")
    @ApiModelProperty(value = "schedule方式")
    private Schedule schedule;

    public Push getPush() {
        return push;
    }

    public void setPush(Push push) {
        this.push = push;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    @Override
    public String toString() {
        return "TriggerRules{" +
                "push=" + push +
                ", schedule=" + schedule +
                '}';
    }
}
