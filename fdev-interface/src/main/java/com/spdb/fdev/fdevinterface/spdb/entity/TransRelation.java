package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

@Document(collection = Dict.INTERFACE_TRANS_RELATION)
public class TransRelation implements Serializable {

    private static final long serialVersionUID = -6811404429965871268L;

    @Id
    private String id;

    // 交易ID
    @Field(Dict.TRANS_ID)
    private String transId;

    // 服务调用方
    @Field(Dict.SERVICE_CALLING)
    private String serviceCalling;

    // 服务提供方
    @Field(Dict.SERVICE_ID)
    private String serviceId;

    // 调用方分支
    @Field(Dict.BRANCH)
    private String branch;

    //请求渠道： client或ajson
    @Field(Dict.CHANNEL)
    private String channel;

    // 创建时间
    @Field(Dict.CREATE_TIME)
    private String createTime;

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

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

}
