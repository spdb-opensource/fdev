package com.spdb.fdev.fdevtask.base.enums;

import com.spdb.fdev.fdevtask.base.annotation.BaseEnum;

/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-03-18 18:34
 **/
public enum  PriorityEnum implements BaseEnum<Integer> {
    LOW(0, "低"),
    MIDDLE(1, "中"),
    HIGH(2, "高"),
    SKY_HIGH(3, "极高");

    /**
     * 枚举值
     */
    private final Integer value;
    /**
     * 操作类型
     */
    private final String opName;

    PriorityEnum(Integer value, String opName) {
        this.value = value;
        this.opName = opName;
    }


    public String getOpName() {
        return opName;
    }

    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return opName;
    }
}
