package com.spdb.fdev.fdevinterface.spdb.vo;

import java.util.List;
import java.util.Map;

/**
 * 交易列表显示层对象
 */
public class TransShow {

    private String id;

    private String appId;

    // 交易ID
    private String transId;

    // 交易名称
    private String transName;

    // 应用名
    private String serviceId;

    // 分支
    private String branch;

    //请求渠道：client html http ajson
    private List<String> channelList;

    // client ajson 的详情ID
    private Map<String, String> channelIdMap;

    // 是否需要登录 0为否，1为是
    private Integer needLogin;

    // 是否记录流水 0为否，1为是
    private Integer writeJnl;

    // 图片验证码
    private String verCodeType;

    // 标签list
    private List<String> tags;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public List<String> getChannelList() {
        return channelList;
    }

    public void setChannelList(List<String> channelList) {
        this.channelList = channelList;
    }

    public Map<String, String> getChannelIdMap() {
        return channelIdMap;
    }

    public void setChannelIdMap(Map<String, String> channelIdMap) {
        this.channelIdMap = channelIdMap;
    }

    public Integer getNeedLogin() {
        return needLogin;
    }

    public void setNeedLogin(Integer needLogin) {
        this.needLogin = needLogin;
    }

    public Integer getWriteJnl() {
        return writeJnl;
    }

    public void setWriteJnl(Integer writeJnl) {
        this.writeJnl = writeJnl;
    }

    public String getVerCodeType() {
        return verCodeType;
    }

    public void setVerCodeType(String verCodeType) {
        this.verCodeType = verCodeType;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

}
