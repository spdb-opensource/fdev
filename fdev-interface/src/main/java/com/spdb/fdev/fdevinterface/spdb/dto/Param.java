package com.spdb.fdev.fdevinterface.spdb.dto;

import java.util.List;

/**
 * 接口请求头、响应头、请求体、响应体的参数对象
 */
public class Param {

    // 参数名称
    private String name;
    // 参数描述
    private String description;
    // 参数类型
    private String type;
    // 是否必传，0：否 1：是
    private Integer required;
    // 参数别名
    private String alias;
    //最大长度
    private Integer maxLength;
    // 备注
    private String remark;

    private List<Param> paramList;

    public Param() {
    }

    public Param(String name, String description, String type, Integer required, String alias) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.required = required;
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getRequired() {
        return required;
    }

    public void setRequired(Integer required) {
        this.required = required;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public List<Param> getParamList() {
        return paramList;
    }

    public void setParamList(List<Param> paramList) {
        this.paramList = paramList;
    }

}
