package com.spdb.fdev.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.base.dict.Dict;
import org.bouncycastle.cms.PasswordRecipientId;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Author:guanz2
 * @Date:2021/9/29-14:49
 * @Description:蓝鲸 pod 数据信息
 */

@Document(collection = Dict.CAASPOD)
public class CaasPod {
    private static final long serialVersionUID = 8637314009320661722L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("cluster")
    private String cluster;

    @Field("namespace")
    private String namespace;

    @Field("deployment")
    private String deployment;

    @Field("name")
    private String name;

    @Field("hostname")
    private String hostname;

    @Field("ip")
    private String ip;

    @Field("area")
    private String area;

    @Field("last_modified_date")
    private String last_modified_date;

    public CaasPod(){

    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getLast_modified_date() {
        return last_modified_date;
    }

    public void setLast_modified_date(String last_modified_date) {
        this.last_modified_date = last_modified_date;
    }

    @Override
    public String toString() {
        return "CaasPod{" +
                "_id=" + _id +
                ", cluster='" + cluster + '\'' +
                ", namespace='" + namespace + '\'' +
                ", deployment='" + deployment + '\'' +
                ", name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", ip='" + ip + '\'' +
                ", area='" + area + '\'' +
                ", last_modified_date='" + last_modified_date + '\'' +
                '}';
    }
}
