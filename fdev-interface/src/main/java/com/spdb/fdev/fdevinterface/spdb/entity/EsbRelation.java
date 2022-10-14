package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(Dict.INTERFACE_ESB_RELATION)
public class EsbRelation implements Serializable {

    private static final long serialVersionUID = 3977436938428689835L;

    @Id
    private String id;

    @Field(Dict.SERVICE_AND_OPERATION_ID)
    private String serviceAndOperationId;

    @Field(Dict.SERVICE_ID)
    private String serviceId;

    @Field(Dict.SERVICE_NAME)
    private String serviceName;

    @Field(Dict.OPERATION_ID)
    private String operationId;

    @Field(Dict.OPERATION_NAME)
    private String operationName;

    @Field(Dict.TRAN_ID)
    private String tranId;

    @Field(Dict.TRAN_NAME)
    private String tranName;

    @Field(Dict.CONSUMER_SYS_ID_AND_NAME)
    private String consumerSysIdAndName;

    @Field(Dict.PROVIDER_SYS_ID_AND_NAME)
    private String providerSysIdAndName;

    @Field(Dict.CONSUMER_MSG_TYPE)
    private String consumerMsgType;

    @Field(Dict.PROVIDER_MSG_TYPE)
    private String providerMsgType;

    @Field(Dict.STATE)
    private String state;

    @Field(Dict.ONLINE_VER)
    private String onlineVer;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getTranId() {
        return tranId;
    }

    public void setTranId(String tranId) {
        this.tranId = tranId;
    }

    public String getTranName() {
        return tranName;
    }

    public void setTranName(String tranName) {
        this.tranName = tranName;
    }

    public String getConsumerSysIdAndName() {
        return consumerSysIdAndName;
    }

    public void setConsumerSysIdAndName(String consumerSysIdAndName) {
        this.consumerSysIdAndName = consumerSysIdAndName;
    }

    public String getProviderSysIdAndName() {
        return providerSysIdAndName;
    }

    public void setProviderSysIdAndName(String providerSysIdAndName) {
        this.providerSysIdAndName = providerSysIdAndName;
    }

    public String getConsumerMsgType() {
        return consumerMsgType;
    }

    public void setConsumerMsgType(String consumerMsgType) {
        this.consumerMsgType = consumerMsgType;
    }

    public String getProviderMsgType() {
        return providerMsgType;
    }

    public void setProviderMsgType(String providerMsgType) {
        this.providerMsgType = providerMsgType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getOnlineVer() {
        return onlineVer;
    }

    public void setOnlineVer(String onlineVer) {
        this.onlineVer = onlineVer;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

}
