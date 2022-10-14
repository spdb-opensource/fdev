package com.spdb.fdev.fdevenvconfig.spdb.entity;

import java.util.Set;

/**
 * create by Idea
 *
 * @Author aric
 * @Date 2020/1/6 17:37
 * @Version 1.0
 */
public class ModelField {

    private String modelName;
    private Set<Object> field;

    public ModelField() {
    }

    public ModelField(String modelName, Set<Object> field) {
        this.modelName = modelName;
        this.field = field;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Set<Object> getField() {
        return field;
    }

    public void setField(Set<Object> field) {
        this.field = field;
    }

    @Override
    public String toString() {
        return "ModelField{" +
                "modelName='" + modelName + '\'' +
                ", field=" + field +
                '}';
    }
}
