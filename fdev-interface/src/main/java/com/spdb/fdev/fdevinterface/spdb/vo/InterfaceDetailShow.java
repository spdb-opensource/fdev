package com.spdb.fdev.fdevinterface.spdb.vo;

import com.spdb.fdev.fdevinterface.spdb.dto.Param;

import java.util.List;

/**
 * 接口详情显示层对象
 */
public class InterfaceDetailShow {

    private String id;

    // 返给前端的交易码
    private String transId;

    //关联esb之前的交易码
    private String relTransId;

    // 接口名称
    private String interfaceName;

    // 接口别名
    private String interfaceAlias;

    // ESB服务ID
    private String esbServiceId;

    // ESB服务名称
    private String esbServiceName;

    // ESB操作ID
    private String esbOperationId;

    // ESB操作名称
    private String esbOperationName;

    // 服务ID+操作ID
    private String serviceAndOperationId;

    private String esbState;

    // 服务方
    private String serviceId;

    // url
    private String uri;

    // 分支名称
    private String branch;

    // 版本号
    private Integer ver;

    // 是否为最新
    private Integer isNew;

    // 请求类型
    private String requestType;

    // 请求协议
    private String requestProtocol;

    // 接口类型
    private String interfaceType;

    //接口描述
    private String description;

    // 是否注册，0：未注册，1：已注册
    private Integer register;

    // 请求体
    private List<Param> request;

    // 响应体
    private List<Param> response;

    // 请求头
    private List<Param> reqHeader;

    // 响应头
    private List<Param> rspHeader;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getEsbServiceName() {
        return esbServiceName;
    }

    public void setEsbServiceName(String esbServiceName) {
        this.esbServiceName = esbServiceName;
    }

    public String getEsbOperationId() {
        return esbOperationId;
    }

    public void setEsbOperationId(String esbOperationId) {
        this.esbOperationId = esbOperationId;
    }

    public String getEsbOperationName() {
        return esbOperationName;
    }

    public void setEsbOperationName(String esbOperationName) {
        this.esbOperationName = esbOperationName;
    }

    public String getServiceAndOperationId() {
        return serviceAndOperationId;
    }

    public void setServiceAndOperationId(String serviceAndOperationId) {
        this.serviceAndOperationId = serviceAndOperationId;
    }

    public String getEsbState() {
        return esbState;
    }

    public void setEsbState(String esbState) {
        this.esbState = esbState;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public Integer getVer() {
        return ver;
    }

    public void setVer(Integer ver) {
        this.ver = ver;
    }

    public Integer getIsNew() {
        return isNew;
    }

    public void setIsNew(Integer isNew) {
        this.isNew = isNew;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestProtocol() {
        return requestProtocol;
    }

    public void setRequestProtocol(String requestProtocol) {
        this.requestProtocol = requestProtocol;
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

    public Integer getRegister() {
        return register;
    }

    public void setRegister(Integer register) {
        this.register = register;
    }

    public List<Param> getRequest() {
        return request;
    }

    public void setRequest(List<Param> request) {
        this.request = request;
    }

    public List<Param> getResponse() {
        return response;
    }

    public void setResponse(List<Param> response) {
        this.response = response;
    }

    public List<Param> getReqHeader() {
        return reqHeader;
    }

    public void setReqHeader(List<Param> reqHeader) {
        this.reqHeader = reqHeader;
    }

    public List<Param> getRspHeader() {
        return rspHeader;
    }

    public void setRspHeader(List<Param> rspHeader) {
        this.rspHeader = rspHeader;
    }

    public String getRelTransId() {
        return relTransId;
    }

    public void setRelTransId(String relTransId) {
        this.relTransId = relTransId;
    }
}
