package com.spdb.fdev.codeReview.spdb.dto;


/**
 * @Author liux81
 * @DATE 2022/1/12
 */
public class ApplyInfo {
    private String applyUser;//申请人
    private String applyTime;//申请时间
    private String applyContent;//申请内容

    public ApplyInfo() {
    }

    public ApplyInfo(String applyUser, String applyTime, String applyContent) {
        this.applyUser = applyUser;
        this.applyTime = applyTime;
        this.applyContent = applyContent;
    }

    public String getApplyUser() {
        return applyUser;
    }

    public void setApplyUser(String applyUser) {
        this.applyUser = applyUser;
    }

    public String getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(String applyTime) {
        this.applyTime = applyTime;
    }

    public String getApplyContent() {
        return applyContent;
    }

    public void setApplyContent(String applyContent) {
        this.applyContent = applyContent;
    }
}
