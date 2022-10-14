package com.spdb.fdev.release.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Document(collection ="prod_appscale")
@ApiModel(value = "变更-应用扩展表")
@CompoundIndexes({
        @CompoundIndex(name = "prod_appscale_idx", def = "{'prod_id':1,'application_id':1,'prod_sequence':1}",unique = true)
})
public class ProdAppScale implements Serializable {

    private static final long serialVersionUID = 1291246662438805313L;

    @Field("prod_id")
    @ApiModelProperty(value = "变更id")
    private String prod_id;

    @Field("application_id")
    @ApiModelProperty(value = "应用id")
    private String application_id;

    @Field("prod_sequence")
    @ApiModelProperty(value = "变更批次")
    private String prod_sequence;

    @Field("tag_name")
    @ApiModelProperty(value = "扩展应用的分支")
    private String tag_name;

    @Field("pro_image_uri")
    @ApiModelProperty(value = "最近一次投产的caas镜像名称")
    private String pro_image_uri;

    @Field("pro_scc_image_uri")
    @ApiModelProperty(value = "最近一次投产的scc镜像名称")
    private String pro_scc_image_uri;

    @Field("env_scales")
    @ApiModelProperty(value = "各环境扩展名")
    private List<Map<String, Object>> env_scales;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    @Field("deploy_type")
    @ApiModelProperty(value="部署平台")
    private List<String> deploy_type;

    public ProdAppScale(){
        super();
    }

    public ProdAppScale(String prod_id, String application_id, List<Map<String, Object>> env_scales,List<String> deploy_type) {
        this.prod_id = prod_id;
        this.application_id = application_id;
        this.env_scales = env_scales;
        this.deploy_type = deploy_type;
    }

    public List<String> getDeploy_type() {
        return deploy_type;
    }

    public void setDeploy_type(List<String> deploy_type) {
        this.deploy_type = deploy_type;
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

    public String getProd_sequence() {
        return prod_sequence;
    }

    public void setProd_sequence(String prod_sequence) {
        this.prod_sequence = prod_sequence;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getPro_image_uri() {
        return pro_image_uri;
    }

    public void setPro_image_uri(String pro_image_uri) {
        this.pro_image_uri = pro_image_uri;
    }

    public List<Map<String, Object>> getEnv_scales() {
        return env_scales;
    }

    public void setEnv_scales(List<Map<String, Object>> env_scales) {
        this.env_scales = env_scales;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getPro_scc_image_uri() {
        return pro_scc_image_uri;
    }

    public void setPro_scc_image_uri(String pro_scc_image_uri) {
        this.pro_scc_image_uri = pro_scc_image_uri;
    }
}