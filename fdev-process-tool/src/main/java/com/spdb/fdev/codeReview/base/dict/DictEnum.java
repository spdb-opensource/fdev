package com.spdb.fdev.codeReview.base.dict;

public enum DictEnum {

    OVERDUE_TYPE("overdueType", "超期类型");

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
