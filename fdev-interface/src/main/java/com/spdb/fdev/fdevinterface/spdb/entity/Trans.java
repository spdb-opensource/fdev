package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = Dict.INTERFACE_TRANS)
public class Trans implements Serializable {

    private static final long serialVersionUID = -4529786828149734628L;

    @Id
    private String id;

    // 交易码
    @Field(Dict.TRANS_ID)
    private String transId;

    // 交易名称
    @Field(Dict.TRANS_NAME)
    private String transName;

    // 服务Id
    @Field(Dict.SERVICE_ID)
    private String serviceId;

    // 分支
    @Field(Dict.BRANCH)
    private String branch;

    //请求渠道： client html http
    @Field(Dict.CHANNEL_LIST)
    private List<String> channelList;

    @Field(Dict.CHANNEL)
    private String channel;

    // 请求类型：defult , json/wsdl , json/xml , xml
    @Field(Dict.REQUEST_TYPE)
    private String requestType;

    //请求协议
    @Field(Dict.REQ_PROTOCOL)
    private String reqProtocol;

    // 是否需要登录 0为否，1为是
    @Field(Dict.NEED_LOGIN)
    private Integer needLogin;

    // 是否记录流水 0为否，1为是
    @Field(Dict.WRITE_JNL)
    private Integer writeJnl;

    // 验证码类型
    @Field(Dict.VER_CODE_TYPE)
    private String verCodeType;

    // 请求体
    @Field(Dict.L_REQUEST)
    private transient List<Map<String, Object>> request;

    // 响应体
    @Field(Dict.L_RESPONSE)
    private transient List<Map<String, Object>> response;

    // 请求头
    @Field(Dict.REQ_HEADER)
    private transient List<Map<String, Object>> reqHeader;

    // 响应头
    @Field(Dict.RSP_HEADER)
    private transient List<Map<String, Object>> rspHeader;

    // 标签list
    @Field(Dict.TAGS)
    private List<String> tags;

    // 该交易所在的文件名称
    @Field(Dict.FILE_NAME)
    private String fileName;

    // 该交易的模板Id
    @Field(Dict.TEMPLATE)
    private String templateId;

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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getReqProtocol() {
        return reqProtocol;
    }

    public void setReqProtocol(String reqProtocol) {
        this.reqProtocol = reqProtocol;
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

    public List<Map<String, Object>> getRequest() {
        return request;
    }

    public void setRequest(List<Map<String, Object>> request) {
        this.request = request;
    }

    public List<Map<String, Object>> getResponse() {
        return response;
    }

    public void setResponse(List<Map<String, Object>> response) {
        this.response = response;
    }

    public List<Map<String, Object>> getReqHeader() {
        return reqHeader;
    }

    public void setReqHeader(List<Map<String, Object>> reqHeader) {
        this.reqHeader = reqHeader;
    }

    public List<Map<String, Object>> getRspHeader() {
        return rspHeader;
    }

    public void setRspHeader(List<Map<String, Object>> rspHeader) {
        this.rspHeader = rspHeader;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
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
