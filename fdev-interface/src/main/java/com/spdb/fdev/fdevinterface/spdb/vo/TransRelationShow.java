package com.spdb.fdev.fdevinterface.spdb.vo;

/**
 * 交易关系显示层对象
 */
public class TransRelationShow {

    private String id;

    // 交易详情Id
    private String transDetailId;

    // 交易ID
    private String transId;

    // 交易名称
    private String transName;

    // 服务调用方
    private String serviceCalling;

    // 服务提供方
    private String serviceId;

    // 服务调用方应用Id
    private String callingAppId;

    // 服务提供方应用Id
    private String serviceProviderAppId;

    // 分支名称
    private String branch;

    private String channel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransDetailId() {
        return transDetailId;
    }

    public void setTransDetailId(String transDetailId) {
        this.transDetailId = transDetailId;
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

    public String getServiceCalling() {
        return serviceCalling;
    }

    public void setServiceCalling(String serviceCalling) {
        this.serviceCalling = serviceCalling;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCallingAppId() {
        return callingAppId;
    }

    public void setCallingAppId(String callingAppId) {
        this.callingAppId = callingAppId;
    }

    public String getServiceProviderAppId() {
        return serviceProviderAppId;
    }

    public void setServiceProviderAppId(String serviceProviderAppId) {
        this.serviceProviderAppId = serviceProviderAppId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

}
