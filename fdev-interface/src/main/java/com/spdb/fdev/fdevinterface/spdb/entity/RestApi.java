package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = Dict.INTERFACE_REST_API)
public class RestApi implements Serializable {

    private static final long serialVersionUID = -375026996497650963L;

    @Id
    private String id;

    // 交易码
    @Field(Dict.TRANS_ID)
    private String transId;

    // 接口名称
    @Field(Dict.INTERFACE_NAME)
    private String interfaceName;

    // 服务方
    @Field(Dict.SERVICE_ID)
    private String serviceId;

    // 版本号
    @Field(Dict.VER)
    private Integer ver;

    // 是否为最新版本，0：否，1：是
    @Field(Dict.IS_NEW)
    private Integer isNew;

    // 分支名称
    @Field(Dict.BRANCH)
    private String branch;

    @Field(Dict.L_URI)
    private String uri;

    // 请求类型
    @Field(Dict.REQUEST_TYPE)
    private String requestType;

    // 请求协议
    @Field(Dict.REQ_PROTOCOL)
    private String requestProtocol;

    // 描述
    @Field(Dict.DESCRIPTION)
    private String description;

    // 请求体
    @Field(Dict.L_REQUEST)
    private transient List<Param> request;

    // 响应体
    @Field(Dict.L_RESPONSE)
    private transient List<Param> response;

    // 请求头
    @Field(Dict.REQ_HEADER)
    private transient List<Param> reqHeader;

    // 响应头
    @Field(Dict.RSP_HEADER)
    private transient List<Param> rspHeader;

    // 是否注册，0：未注册，1：已注册
    @Field(Dict.REGISTER)
    private Integer register;

    // GitLab上对应json文件的Repository ID
    @Field(Dict.REPOSITORY_ID)
    private String repositoryId;

    // GitLab上头文件的Repository ID
    @Field(Dict.HEADER_REPOSITORY_ID)
    private String headerRepositoryId;

    // GitLab上头文件的Repository ID
    @Field(Dict.SCHEMA_CONFIG_REPOSITORY_ID)
    private String schemaConfigRepositoryId;

    @Field(Dict.L_INTERFACE_TYPE)
    private String interfaceType;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

    // 更新时间
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

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getRegister() {
        return register;
    }

    public void setRegister(Integer register) {
        this.register = register;
    }

    public String getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(String repositoryId) {
        this.repositoryId = repositoryId;
    }

    public String getHeaderRepositoryId() {
        return headerRepositoryId;
    }

    public void setHeaderRepositoryId(String headerRepositoryId) {
        this.headerRepositoryId = headerRepositoryId;
    }

    public String getSchemaConfigRepositoryId() {
        return schemaConfigRepositoryId;
    }

    public void setSchemaConfigRepositoryId(String schemaConfigRepositoryId) {
        this.schemaConfigRepositoryId = schemaConfigRepositoryId;
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

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

}
