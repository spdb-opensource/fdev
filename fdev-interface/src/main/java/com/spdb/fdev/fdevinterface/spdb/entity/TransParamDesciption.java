package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(Dict.INTERFACE_TRANS_PARAM_DESCRIPTION)
public class TransParamDesciption implements Serializable {

    private static final long serialVersionUID = 3977436938428689835L;

    @Id
    private String id;

    // 交易码
    @Field(Dict.TRANS_ID)
    private String transId;

    // 服务Id
    @Field(Dict.SERVICE_ID)
    private String serviceId;


    @Field(Dict.CHANNEL)
    private String channel;

    // 请求体
    @Field(Dict.L_REQUEST)
    private transient List<Map<String, Object>> request;

    // 响应体
    @Field(Dict.L_RESPONSE)
    private transient List<Map<String, Object>> response;

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

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }


    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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
