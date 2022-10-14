package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JobInfo {

    @Field("image")
    private Images image;

    @Field("variables")
    private List<Map<String, String>> variables;

    public Images getImage() {
        return image;
    }

    public void setImage(Images image) {
        this.image = image;
    }

    public List<Map<String, String>> getVariables() {
        return variables;
    }

    public void setVariables(List<Map<String, String>> variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return "JobInfo{" +
                "image=" + image +
                ", variables=" + variables +
                '}';
    }
}
