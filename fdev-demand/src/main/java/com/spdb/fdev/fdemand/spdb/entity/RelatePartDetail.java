package com.spdb.fdev.fdemand.spdb.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.spdb.fdev.fdemand.base.utils.CommonUtils;

public class RelatePartDetail {

    //该条数据会包含牵头板块。
    /**
     * 板块id（牵头板块会默认展示在涉及板块中。）
     */
    private String part_id;

    /**
     * 板块名称
     */
    private String part_name;

    /**
     * 评估状态（预评估、评估中、评估完成）
     */
    private String assess_status;

    /**
     * 评估人员，多选（参照牵头人数据类型）
     */
    private HashSet<String> assess_user;

    /**
     * 评估人员
     */
    private List<UserInfo> assess_user_all;

    public String getPart_id() {
        return part_id;
    }

    public void setPart_id(String part_id) {
        this.part_id = part_id;
    }

    public String getPart_name() {
        return part_name;
    }

    public void setPart_name(String part_name) {
        this.part_name = part_name;
    }

    public String getAssess_status() {
        return assess_status;
    }

    public void setAssess_status(String assess_status) {
        this.assess_status = assess_status;
    }

    public HashSet<String> getAssess_user() {
    	if(CommonUtils.isNullOrEmpty(assess_user)) {
    		return new HashSet();
    	}
        return assess_user;
    }

    public void setAssess_user(HashSet<String> assess_user) {
        this.assess_user = assess_user;
    }

    public List<UserInfo> getAssess_user_all() {
    	if(CommonUtils.isNullOrEmpty(assess_user_all)) {
    		return new ArrayList();
    	}
        return assess_user_all;
    }

    public void setAssess_user_all(List<UserInfo> assess_user_all) {
        this.assess_user_all = assess_user_all;
    }
}
