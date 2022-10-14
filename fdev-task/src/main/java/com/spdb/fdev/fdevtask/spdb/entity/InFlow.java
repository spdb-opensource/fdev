package com.spdb.fdev.fdevtask.spdb.entity;

import java.util.List;

/**
 * @program: fdev-process
 * @description:
 * @author: c-gaoys
 * @create: 2021-01-28 11:19
 **/
public class InFlow {
    /**
     * 状态id
     */
    private String fdevProcessStatusId;

    /**
     * 准入条件
     */
    private List<String> checkFieldList;

    public InFlow() {
    }

    public InFlow(String fdevProcessStatusId) {
        this.fdevProcessStatusId = fdevProcessStatusId;
    }

    public String getFdevProcessStatusId() {
        return fdevProcessStatusId;
    }

    public void setFdevProcessStatusId(String fdevProcessStatusId) {
        this.fdevProcessStatusId = fdevProcessStatusId;
    }

    public List<String> getCheckFieldList() {
        return checkFieldList;
    }

    public void setCheckFieldList(List<String> checkFieldList) {
        this.checkFieldList = checkFieldList;
    }
}
