package com.spdb.fdev.fdemand.base.dict;

public enum DictEnum {

    OVERDUE_TYPE("overdueType", "超期类型"),
    CONF_STATE("confState", "conf同步数据约定状态");

    private String type;
    private String typeName;

    DictEnum(String type, String typeName) {
        this.type = type;
        this.typeName = typeName;
    }

    public String getType() {
        return type;
    }

    public String getTypeName() {
        return typeName;
    }

}
