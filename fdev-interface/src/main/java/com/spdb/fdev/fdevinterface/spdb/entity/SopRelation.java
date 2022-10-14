package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = Dict.INTERFACE_SOP_RELATION)
public class SopRelation implements Serializable {

    private static final long serialVersionUID = -4334047657771647269L;

    @Id
    private String id;

    // 交易码
    @Field(Dict.TRANS_ID)
    private String transId;

    // 接口名称
    @Field(Dict.INTERFACE_NAME)
    private String interfaceName;

    // 接口别名
    @Field(Dict.INTERFACE_ALIAS)
    private String interfaceAlias;

    // 服务调用方
    @Field(Dict.SERVICE_CALLING)
    private String serviceCalling;

    // 服务提供方
    @Field(Dict.SERVICE_ID)
    private String serviceId;

    @Field(Dict.L_URI)
    private String uri;

    // 分支名
    @Field(Dict.BRANCH)
    private String branch;

    // 版本号
    @Field(Dict.VER)
    private Integer ver;

    // 是否最新，0：不是最新版本 1：是最新版本
    @Field(Dict.IS_NEW)
    private Integer isNew;

    // 请求类型
    @Field(Dict.REQUEST_TYPE)
    private String requestType;

    // 请求协议
    @Field(Dict.REQ_PROTOCOL)
    private String requestProtocol;

    // 请求头
    @Field(Dict.REQ_HEADER)
    private transient List<Param> reqHeader;

    // 响应头
    @Field(Dict.RSP_HEADER)
    private transient List<Param> rspHeader;

    // 请求体
    @Field(Dict.L_REQUEST)
    private transient List<Param> request;

    // 响应体
    @Field(Dict.L_RESPONSE)
    private transient List<Param> response;

    // GitLab上对应文件的Repository ID
    @Field(Dict.REPOSITORY_ID)
    private String repositoryId;

    @Field(Dict.L_INTERFACE_TYPE)
    private String interfaceType;

    @Field(Dict.ESB_TRANS_ID)
    private String esbTransId;

    @Field(Dict.ESB_SERVICE_ID)
    private String esbServiceId;

    @Field(Dict.ESB_OPERATION_ID)
    private String esbOperationId;

    @Field(Dict.CREATE_TIME)
    private String createTime;

    @Field(Dict.UPDATE_TIME)
    private String updateTime;

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

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getEsbTransId() {
        return esbTransId;
    }

    public void setEsbTransId(String esbTransId) {
        this.esbTransId = esbTransId;
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
}
