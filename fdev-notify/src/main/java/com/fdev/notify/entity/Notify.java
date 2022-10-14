package com.fdev.notify.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fdev.notify.util.NotifyUtil;

import java.io.Serializable;

public class Notify implements Serializable {

    private String content;

    private String module;

    private String target;

    public Notify() {
    }

    public Notify(String content, String module, String target) {
        this.content = content;
        this.module = module;
        this.target = target;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String toString() {
        String json = "{}";
        try {
            json = NotifyUtil.objectToJson(this);
        } catch (JsonProcessingException e) {
            return "{}";
        }
        return json;
    }
}
