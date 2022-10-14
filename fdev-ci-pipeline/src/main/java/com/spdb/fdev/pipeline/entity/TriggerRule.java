package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TriggerRule {


    @Field("branches")
    private List<String> branches;            //分支名规则

    @Field("env")
    private EnvInfo env;                //所选环境

    public List<String> getBranches() {
        return branches;
    }

    public void setBranches(List<String> branches) {
        this.branches = branches;
    }

    public EnvInfo getEnv() {
        return env;
    }

    public void setEnv(EnvInfo env) {
        this.env = env;
    }
}
