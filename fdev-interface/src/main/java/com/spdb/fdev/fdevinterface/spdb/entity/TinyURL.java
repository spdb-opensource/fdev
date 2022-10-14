package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Document(collection = Dict.INTERFACE_TINY_URL)
public class TinyURL implements Serializable {

    private static final long serialVersionUID = -5314007792163404742L;

    @Id
    private String id;

    // 响应头
    @Field(Dict.TINY_URL_KEY)
    private transient Map<String, Object> tinyUrlkey;

    // 生成日期
    @Field(Dict.CREATE_TIME)
    private Date createTime;

    // 服务名
    @Field(Dict.SERVICE_ID)
    private String serviceId;

    public TinyURL(Map<String, Object> tinyUrlkey, Date createTime, String serviceId) {
        super();
        this.tinyUrlkey = tinyUrlkey;
        this.createTime = createTime;
        this.serviceId = serviceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, Object> getTinyUrlkey() {
        return tinyUrlkey;
    }

    public void setTinyUrlkey(Map<String, Object> tinyUrlkey) {
        this.tinyUrlkey = tinyUrlkey;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

}
