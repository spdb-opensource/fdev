package com.spdb.fdev.fdevinterface.spdb.vo;

/**
 * 接口列表显示层对象
 */
public class InterfaceShow {

    private String id;

    // 应用id
    private String appId;

    // 交易码
    private String transId;

    // 接口名称
    private String interfaceName;

    // 接口别名
    private String interfaceAlias;

    // ESB服务ID
    private String esbServiceId;

    // ESB操作ID
    private String esbOperationId;

    // 服务ID+操作ID
    private String serviceAndOperationId;

    // 服务方
    private String serviceId;

    // 分支名称
    private String branch;

    // 请求类型
    private String requestType;

    // 接口类型
    private String interfaceType;

    //接口描述
    private String description;

    private String requestProtocol;

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

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getInterfaceAlias() {
        return interfaceAlias;
    }

    public void setInterfaceAlias(String interfaceAlias) {
        this.interfaceAlias = interfaceAlias;
    }

    public String getEsbServiceId() {
        return esbServiceId;
    }

    public void setEsbServiceId(String esbServiceId) {
        this.esbServiceId = esbServiceId;
    }

    public String getEsbOperationId() {
        return esbOperationId;
    }

    public void setEsbOperationId(String esbOperationId) {
        this.esbOperationId = esbOperationId;
    }

    public String getServiceAndOperationId() {
        return serviceAndOperationId;
    }

    public void setServiceAndOperationId(String serviceAndOperationId) {
        this.serviceAndOperationId = serviceAndOperationId;
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

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
    }

}
