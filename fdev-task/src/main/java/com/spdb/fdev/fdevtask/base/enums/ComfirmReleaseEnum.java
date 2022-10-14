package com.spdb.fdev.fdevtask.base.enums;

import com.spdb.fdev.fdevtask.base.annotation.BaseEnum;

/**
 * @program: fdev-task
 * @description:
 * @author: c-jiangl2
 * @create: 2021-03-18 18:34
 **/
public enum ComfirmReleaseEnum implements BaseEnum {
    NO_RELEASE("0", "未确认发布"),
    RELEASE("1", "确认发布");

    /**
     * 枚举值
     */
    private final String value;
    /**
     * 操作类型
     */
    private final String opName;

    ComfirmReleaseEnum(String value, String opName) {
        this.value = value;
        this.opName = opName;
    }


    public String getOpName() {
        return opName;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String getName() {
        return opName;
    }
}
