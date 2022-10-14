package com.spdb.fdev.fdevapp.spdb.entity;

import java.io.Serializable;

/**
 * @author xxx
 * @date 2019/4/2 10:01
 */
public class EnvBranch implements Serializable {
    private String name;
    private String auto;
    private String schedule;
    private String auto_env_name;
    private String schedule_env_name;

    public String getAuto_env_name() {
        return auto_env_name;
    }

    public void setAuto_env_name(String auto_env_name) {
        this.auto_env_name = auto_env_name;
    }

    public String getSchedule_env_name() {
        return schedule_env_name;
    }

    public void setSchedule_env_name(String schedule_env_name) {
        this.schedule_env_name = schedule_env_name;
    }

    public EnvBranch() {
    }

    public EnvBranch(String name, String auto, String schedule) {
        this.name = name;
        this.auto = auto;
        this.schedule = schedule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuto() {
        return auto;
    }

    public void setAuto(String auto) {
        this.auto = auto;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }
}
