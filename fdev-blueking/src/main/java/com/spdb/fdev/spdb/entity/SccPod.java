package com.spdb.fdev.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @Author:guanz2
 * @Date:2021/10/3-17:03
 * @Description: scc pod 实体类
 */
@Document(collection = Dict.SCCPOD)
public class SccPod {

    private static final long serialVersionUID = 8637314009320661715L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("cluster_code")
    private String cluster_code;

    @Field("namespace_code")
    private String namespace_code;

    @Field("owner_code")
    private String owner_code;

    @Field("pod_code")
    private String pod_code;

    @Field("pod_ip")
    private String pod_ip;

    @Field("node_name")
    private String node_name;

    @Field("last_modified_date")
    private String last_modified_date;

    public SccPod(){

    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getCluster_code() {
        return cluster_code;
    }

    public void setCluster_code(String cluster_code) {
        this.cluster_code = cluster_code;
    }

    public String getNamespace_code() {
        return namespace_code;
    }

    public void setNamespace_code(String namespace_code) {
        this.namespace_code = namespace_code;
    }

    public String getOwner_code() {
        return owner_code;
    }

    public void setOwner_code(String owner_code) {
        this.owner_code = owner_code;
    }

    public String getPod_code() {
        return pod_code;
    }

    public void setPod_code(String pod_code) {
        this.pod_code = pod_code;
    }

    public String getPod_ip() {
        return pod_ip;
    }

    public void setPod_ip(String pod_ip) {
        this.pod_ip = pod_ip;
    }

    public String getNode_name() {
        return node_name;
    }

    public void setNode_name(String node_name) {
        this.node_name = node_name;
    }

    public String getLast_modified_date() {
        return last_modified_date;
    }

    public void setLast_modified_date(String last_modified_date) {
        this.last_modified_date = last_modified_date;
    }

    @Override
    public String toString() {
        return "SccPod{" +
                "_id=" + _id +
                ", cluster_code='" + cluster_code + '\'' +
                ", namespace_code='" + namespace_code + '\'' +
                ", owner_code='" + owner_code + '\'' +
                ", pod_code='" + pod_code + '\'' +
                ", pod_ip='" + pod_ip + '\'' +
                ", node_name='" + node_name + '\'' +
                ", last_modified_date='" + last_modified_date + '\'' +
                '}';
    }
}
