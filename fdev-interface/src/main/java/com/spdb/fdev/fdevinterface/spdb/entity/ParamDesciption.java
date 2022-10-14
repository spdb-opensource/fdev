package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import com.spdb.fdev.fdevinterface.spdb.dto.Param;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(Dict.INTERFACE_PARAM_DESCRIPTION)
public class ParamDesciption implements Serializable {

    private static final long serialVersionUID = 3977436938428689835L;

    @Id
    private String id;

    // 交易码
    @Field(Dict.TRANS_ID)
    private String transId;


    // 服务方
    @Field(Dict.SERVICE_ID)
    private String serviceId;


    // 请求体
    @Field(Dict.L_REQUEST)
    private transient List<Param> request;

    // 响应体
    @Field(Dict.L_RESPONSE)
    private transient List<Param> response;


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


    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
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

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
