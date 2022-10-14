package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = Dict.INTERFACE_TOTAL_JSON)
public class TotalJson implements Serializable {
    private static final long serialVersionUID = -120473464200801287L;

    @Id
    private String id;

    // 环境
    @Field(Dict.ENV)
    private String env;

    // 介质生成时间
    @Field(Dict.DAT_TIME)
    private String datTime;

    // central.json里包括的应用数量
    @Field(Dict.APP_NUM)
    private Integer appNum;

    // central.json文件内容
    @Field(Dict.CENTRAL_JSON)
    private Map<String, Object> centralJson;

    // 介质包名
    @Field(Dict.TOTAL_TAR_NAME)
    private String totalTarName;

    // 介质下载链接
    @Field(Dict.TOTAL_TAR_URL)
    private String totalTarUrl;

    // 是否为最新版本
    @Field(Dict.IS_NEW)
    private Integer isNew;

    @Field(Dict.PROD_ID)
    private String prodId;

    @Field(Dict.PROD_VERSION)
    private String prodVersion;

    // 记录本次变更准备了哪些应用的介质，以空格隔开
    @Field(Dict.APP_TAR_NAME_LIST)
    private String appTarNameList;

    @Field(Dict.APP_JSON_URL_MAP)
    private Map<String,String> appJsonUrlMap;

    // 记录本次变更是否涉及新增应用，1：是   0：否
    @Field(Dict.FLAG)
    private Integer flag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getDatTime() {
        return datTime;
    }

    public void setDatTime(String datTime) {
        this.datTime = datTime;
    }

    public Integer getAppNum() {
        return appNum;
    }

    public void setAppNum(Integer appNum) {
        this.appNum = appNum;
    }

    public Map<String, Object> getCentralJson() {
        return centralJson;
    }

    public void setCentralJson(Map<String, Object> centralJson) {
        this.centralJson = centralJson;
    }

    public String getTotalTarName() {
        return totalTarName;
    }

    public void setTotalTarName(String totalTarName) {
        this.totalTarName = totalTarName;
    }

    public String getTotalTarUrl() {
        return totalTarUrl;
    }

    public void setTotalTarUrl(String totalTarUrl) {
        this.totalTarUrl = totalTarUrl;
    }

    public Integer getIsNew() {
        return isNew;
    }

    public void setIsNew(Integer isNew) {
        this.isNew = isNew;
    }

    public String getProdId() {
        return prodId;
    }

    public void setProdId(String prodId) {
        this.prodId = prodId;
    }

    public String getProdVersion() {
        return prodVersion;
    }

    public void setProdVersion(String prodVersion) {
        this.prodVersion = prodVersion;
    }

    public String getAppTarNameList() {
        return appTarNameList;
    }

    public void setAppTarNameList(String appTarNameList) {
        this.appTarNameList = appTarNameList;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public Map<String, String> getAppJsonUrlMap() {
        return appJsonUrlMap;
    }

    public void setAppJsonUrlMap(Map<String, String> appJsonUrlMap) {
        this.appJsonUrlMap = appJsonUrlMap;
    }
}
