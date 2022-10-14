package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Map;

@Document(Dict.INTERFACE_SCAN_RECORD)
public class ScanRecord implements Serializable {

    private static final long serialVersionUID = -606889591270368336L;

    @Id
    private String id;

    // 应用英文名
    @Field(Dict.SERVICE_ID)
    private String serviceId;

    private String appId;

    // 分支
    @Field(Dict.BRANCH)
    private String branch;

    // 触发扫描的方式
    @Field(Dict.TYPE)
    private String type;

    // REST提供
    @Field(Dict.L_REST)
    private transient Map rest;

    // REST调用
    @Field(Dict.L_REST_REL)
    private transient Map restRel;

    // SOAP提供
    @Field(Dict.L_SOAP)
    private transient Map soap;

    // SOAP调用
    @Field(Dict.L_SOAP_REL)
    private transient Map soapRel;

    // SOP调用
    @Field(Dict.L_SOP_REL)
    private transient Map sopRel;

    // 交易提供
    @Field(Dict.L_TRANS)
    private transient Map trans;

    // 交易调用
    @Field(Dict.L_TRANS_REL)
    private transient Map transRel;
    
    // 路由扫描
    @Field(Dict.L_ROUTER)
    private transient Map router;

    // 扫描时间
    @Field(Dict.SCAN_TIME)
    private String scanTime;
    //所属分组
    private String group;
	public Map getRouter() {
		return router;
	}

	public void setRouter(Map router) {
		this.router = router;
	}

	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map getRest() {
        return rest;
    }

    public void setRest(Map rest) {
        this.rest = rest;
    }

    public Map getRestRel() {
        return restRel;
    }

    public void setRestRel(Map restRel) {
        this.restRel = restRel;
    }

    public Map getSoap() {
        return soap;
    }

    public void setSoap(Map soap) {
        this.soap = soap;
    }

    public Map getSoapRel() {
        return soapRel;
    }

    public void setSoapRel(Map soapRel) {
        this.soapRel = soapRel;
    }

    public Map getSopRel() {
        return sopRel;
    }

    public void setSopRel(Map sopRel) {
        this.sopRel = sopRel;
    }

    public Map getTrans() {
        return trans;
    }

    public void setTrans(Map trans) {
        this.trans = trans;
    }

    public Map getTransRel() {
        return transRel;
    }

    public void setTransRel(Map transRel) {
        this.transRel = transRel;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

}
