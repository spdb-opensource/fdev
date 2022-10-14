package com.spdb.fdev.fdevinterface.spdb.vo;

import java.util.List;

/**
 * 交易前端请求参数对象
 */
public class TransParamShow {

    private String branchDefault;
    private String branch;
    private List<String> transId;
    private String transName;
    private String serviceId;
    private String serviceCalling;
    private String channel;
    private String needLogin;
    private String writeJnl;
    private String verCodeType;
    private String tags;
    private Integer page;
    private Integer pageNum;

    public String getBranchDefault() {
        return branchDefault;
    }

    public void setBranchDefault(String branchDefault) {
        this.branchDefault = branchDefault;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<String> getTransId() {
        return transId;
    }

    public void setTransId(List<String> transId) {
        this.transId = transId;
    }

    public String getTransName() {
        return transName;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(String needLogin) {
        this.needLogin = needLogin;
    }

    public String getWriteJnl() {
        return writeJnl;
    }

    public void setWriteJnl(String writeJnl) {
        this.writeJnl = writeJnl;
    }

    public String getVerCodeType() {
        return verCodeType;
    }

    public void setVerCodeType(String verCodeType) {
        this.verCodeType = verCodeType;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getServiceCalling() {
        return serviceCalling;
    }

    public void setServiceCalling(String serviceCalling) {
        this.serviceCalling = serviceCalling;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

}
