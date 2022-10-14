package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@Document(collection = "esf_registration")
@ApiModel(value = "esf注册信息表")
public class EsfRegistration {

    @Field("esf_id")
    @Indexed
    @ApiModelProperty(value = "esf注册id")
    private String esf_id;

    @Field("prod_id")
    @ApiModelProperty(value = "变更id")
    private String prod_id;

    @Field("application_id")
    @ApiModelProperty(value = "应用id")
    private String application_id;

    @Field("caas_network_area")
    @ApiModelProperty(value = "caas网络模式:Underlay")
    private String caas_network_area;

    @Field("scc_network_area")
    @ApiModelProperty(value = "scc网络模式：Underlay，Overlay")
    private String scc_network_area;

    @Field("sid")
    @ApiModelProperty(value = "应用sid")
    private String sid;

    @Field("platform")
    @ApiModelProperty(value = "部署平台 CAAS,SCC")
    private List<String> platform;

    @Field("upload_user")
    @ApiModelProperty(value = "操作人")
    private String upload_user;

    @Field("upload_username_cn")
    @ApiModelProperty(value = "操作人中文名")
    private String upload_username_cn;

    @Field("update_time")
    @ApiModelProperty(value = "操作时间")
    private String update_time;

    @Field("esf_info")
    @ApiModelProperty(value = "用户信息")
    private Map<String, Object> esf_info;

    @Transient
    @ApiModelProperty(value = "应用英文名")
    private String application_en;

    @Transient
    @ApiModelProperty(value = "应用中文名")
    private String application_cn;

    @Transient
    @ApiModelProperty(value = "SDK-GK")
    private String sdk_gk;


    public EsfRegistration() {
        super();
    }

    public EsfRegistration(String prod_id, String application_id, String caas_network_area, String scc_network_area, String sid, List<String> platform, String upload_user, String upload_username_cn, String  update_time, Map<String, Object> esf_info) {
        this.esf_id = new ObjectId().toString();
        this.prod_id = prod_id;
        this.application_id = application_id;
        this.caas_network_area = caas_network_area;
        this.scc_network_area = scc_network_area;
        this.sid = sid;
        this.platform = platform;
        this.upload_user = upload_user;
        this.upload_username_cn = upload_username_cn;
        this.update_time = update_time;
        this.esf_info = esf_info;
    }

    public String getEsf_id() {
        return esf_id;
    }

    public void setEsf_id(String esf_id) {
        this.esf_id = esf_id;
    }

    public String getProd_id() {
        return prod_id;
    }

    public void setProd_id(String prod_id) {
        this.prod_id = prod_id;
    }

    public String getApplication_id() {
        return application_id;
    }

    public void setApplication_id(String application_id) {
        this.application_id = application_id;
    }

    public String getCaas_network_area() {
        return caas_network_area;
    }

    public void setCaas_network_area(String caas_network_area) {
        this.caas_network_area = caas_network_area;
    }

    public String getScc_network_area() {
        return scc_network_area;
    }

    public void setScc_network_area(String scc_network_area) {
        this.scc_network_area = scc_network_area;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Map<String, Object> getEsf_info() {
        return esf_info;
    }

    public void setEsf_info(Map<String, Object> esf_info) {
        this.esf_info = esf_info;
    }

    public List<String> getPlatform() {
        return platform;
    }

    public void setPlatform(List<String> platform) {
        this.platform = platform;
    }

    public String getApplication_en() {
        return application_en;
    }

    public void setApplication_en(String application_en) {
        this.application_en = application_en;
    }

    public String getSdk_gk() {
        return sdk_gk;
    }

    public void setSdk_gk(String sdk_gk) {
        this.sdk_gk = sdk_gk;
    }

    public String getUpload_user() {
        return upload_user;
    }

    public void setUpload_user(String upload_user) {
        this.upload_user = upload_user;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getApplication_cn() {
        return application_cn;
    }

    public void setApplication_cn(String application_cn) {
        this.application_cn = application_cn;
    }

    public String getUpload_username_cn() {
        return upload_username_cn;
    }

    public void setUpload_username_cn(String upload_username_cn) {
        this.upload_username_cn = upload_username_cn;
    }
}
