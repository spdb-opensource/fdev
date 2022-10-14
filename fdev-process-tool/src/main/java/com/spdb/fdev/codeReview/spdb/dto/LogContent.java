package com.spdb.fdev.codeReview.spdb.dto;

import org.springframework.stereotype.Component;

/**
 * @Author liux81
 * @DATE 2021/11/9
 */
@Component
public class LogContent {

    private String fieldName;
    private Object oldValue;
    private Object newValue;

    public LogContent() {
    }

    public LogContent(String fieldName, Object oldValue, Object newValue) {
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
}
