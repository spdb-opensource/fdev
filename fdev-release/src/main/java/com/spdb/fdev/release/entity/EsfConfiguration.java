package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = "esf_configuration")
@ApiModel(value = "esf配置中心表")

public class EsfConfiguration {

    @Field("env_name")
    @ApiModelProperty(value = "环境目录")
    private String env_name;

    @Field("network")
    @ApiModelProperty(value = "网段")
    private String network;

    @Field("partition")
    @ApiModelProperty(value = "逻辑分区")
    private String partition;


    @Field("bucket_name")
    @ApiModelProperty(value = "桶名")
    private String bucket_name;

    @Field("config_ip")
    @ApiModelProperty(value = "配置中心IP")
    private String config_ip;

    @Field("config_area")
    @ApiModelProperty(value = "配置中心域名")
    private String config_area;

    public String getEnv_name() {
        return env_name;
    }

    public void setEnv_name(String env_name) {
        this.env_name = env_name;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public String getBucket_name() {
        return bucket_name;
    }

    public void setBucket_name(String bucket_name) {
        this.bucket_name = bucket_name;
    }

    public String getConfig_ip() {
        return config_ip;
    }

    public void setConfig_ip(String config_ip) {
        this.config_ip = config_ip;
    }

    public String getConfig_area() {
        return config_area;
    }

    public void setConfig_area(String config_area) {
        this.config_area = config_area;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }
}
