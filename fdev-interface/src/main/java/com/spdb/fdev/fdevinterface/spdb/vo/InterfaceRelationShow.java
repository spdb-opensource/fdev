package com.spdb.fdev.fdevinterface.spdb.vo;

/**
 * 接口关系显示层对象
 */
public class InterfaceRelationShow {

    private String id;

    // 接口Id
    private String interfaceId;

    // 交易码
    private String transId;

    // 接口名称
    private String interfaceName;

    // ESB服务ID
    private String esbServiceId;

    // ESB操作ID
    private String esbOperationId;

    private String esbTransId;

    // 服务ID+操作ID
    private String serviceAndOperationId;

    // 接口别名
    private String interfaceAlias;

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

    // 接口类型
    private String interfaceType;

    private String requestType;

    private String appServiceManagers;

    private String appCallingManagers;

    private String uri;

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

    //接口描述
    private String description;

    private String requestProtocol;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
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

    public String getInterfaceAlias() {
        return interfaceAlias;
    }

    public void setInterfaceAlias(String interfaceAlias) {
        this.interfaceAlias = interfaceAlias;
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public  String getAppServiceManagers() {
        return appServiceManagers;
    }

    public void setAppServiceManagers( String appServiceManagers) {
        this.appServiceManagers = appServiceManagers;
    }

    public String getAppCallingManagers() {
        return appCallingManagers;
    }

    public void setAppCallingManagers( String appCallingManagers) {
        this.appCallingManagers = appCallingManagers;
    }

    public String getUri() {
        return uri;
    }

    public void setUri( String uri) {
        this.uri = uri;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType( String requestType) {
        this.requestType = requestType;
    }

    public String getEsbTransId() {
        return esbTransId;
    }

    public void setEsbTransId(String esbTransId) {
        this.esbTransId = esbTransId;
    }
}
