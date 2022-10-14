package com.spdb.fdev.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.spdb.fdev.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @Author:guanz2
 * @Date:2021/9/29-14:41
 * @Description: 蓝鲸，deployment信息
 */

@Document(collection = Dict.CAASDEPLOYMENT)
public class CaasDeployment implements Serializable {

    private static final long serialVersionUID = 8637314009320661721L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("cluster")
    private String cluster;

    @Field("namespace")
    private String namespace;

    @Field("deployment")
    private String deployment;

    @Field("nodeselectorterms")
    private String nodeselectorterms;

    @Field("area")
    private String area;

    @Field("volumemounts")
    private String volumemounts;

    @Field("dnspolicy")
    private String dnspolicy;

    @Field("dnsconfig")
    private String dnsconfig;

    @Field("tag")
    private String tag;

    @Field("imagepullsecrets")
    private String imagepullsecrets;

    @Field("prestop")
    private String prestop;

    @Field("replicas")
    private String replicas;

    @Field("cpu_limits")
    private String cpu_limits;

    @Field("cpu_requests")
    private String cpu_requests;

    @Field("memory_limits")
    private String memory_limits;

    @Field("memory_requests")
    private String memory_requests;

    @Field("volumes")
    private String volumes;

    @Field("portserver")
    private String portserver;

    @Field("env")
    private String env;

    @Field("envfrom")
    private String envfrom;

    @Field("strategytype")
    private String strategytype;

    @Field("hostalias")
    private String hostalias;

    @Field("last_modified_date")
    private String last_modified_date;

    @Field("allocated_ip_segment")
    private String allocated_ip_segment;

    @Field("readinessprobe")
    private String readinessprobe;

    @Field("initContainers")
    private String initContainers;

    public CaasDeployment(){

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

    public String getNodeselectorterms() {
        return nodeselectorterms;
    }

    public void setNodeselectorterms(String nodeselectorterms) {
        this.nodeselectorterms = nodeselectorterms;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getVolumemounts() {
        return volumemounts;
    }

    public void setVolumemounts(String volumemounts) {
        this.volumemounts = volumemounts;
    }

    public String getDnspolicy() {
        return dnspolicy;
    }

    public void setDnspolicy(String dnspolicy) {
        this.dnspolicy = dnspolicy;
    }

    public String getDnsconfig() {
        return dnsconfig;
    }

    public void setDnsconfig(String dnsconfig) {
        this.dnsconfig = dnsconfig;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getImagepullsecrets() {
        return imagepullsecrets;
    }

    public void setImagepullsecrets(String imagepullsecrets) {
        this.imagepullsecrets = imagepullsecrets;
    }

    public String getPrestop() {
        return prestop;
    }

    public void setPrestop(String prestop) {
        this.prestop = prestop;
    }

    public String getReplicas() {
        return replicas;
    }

    public void setReplicas(String replicas) {
        this.replicas = replicas;
    }

    public String getCpu_limits() {
        return cpu_limits;
    }

    public void setCpu_limits(String cpu_limits) {
        this.cpu_limits = cpu_limits;
    }

    public String getCpu_requests() {
        return cpu_requests;
    }

    public void setCpu_requests(String cpu_requests) {
        this.cpu_requests = cpu_requests;
    }

    public String getMemory_limits() {
        return memory_limits;
    }

    public void setMemory_limits(String memory_limits) {
        this.memory_limits = memory_limits;
    }

    public String getMemory_requests() {
        return memory_requests;
    }

    public void setMemory_requests(String memory_requests) {
        this.memory_requests = memory_requests;
    }

    public String getVolumes() {
        return volumes;
    }

    public void setVolumes(String volumes) {
        this.volumes = volumes;
    }

    public String getPortserver() {
        return portserver;
    }

    public void setPortserver(String portserver) {
        this.portserver = portserver;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getEnvfrom() {
        return envfrom;
    }

    public void setEnvfrom(String envfrom) {
        this.envfrom = envfrom;
    }

    public String getStrategytype() {
        return strategytype;
    }

    public void setStrategytype(String strategytype) {
        this.strategytype = strategytype;
    }

    public String getHostalias() {
        return hostalias;
    }

    public void setHostalias(String hostalias) {
        this.hostalias = hostalias;
    }

    public String getLast_modified_date() {
        return last_modified_date;
    }

    public void setLast_modified_date(String last_modified_date) {
        this.last_modified_date = last_modified_date;
    }

    public String getAllocated_ip_segment() {
        return allocated_ip_segment;
    }

    public void setAllocated_ip_segment(String allocated_ip_segment) {
        this.allocated_ip_segment = allocated_ip_segment;
    }

    public String getReadinessprobe() {
        return readinessprobe;
    }

    public void setReadinessprobe(String readinessprobe) {
        this.readinessprobe = readinessprobe;
    }

    public String getInitContainers() {
        return initContainers;
    }

    public void setInitContainers(String initContainers) {
        this.initContainers = initContainers;
    }

    @Override
    public String toString() {
        return "CaasDeployment{" +
                "_id=" + _id +
                ", cluster='" + cluster + '\'' +
                ", namespace='" + namespace + '\'' +
                ", deployment='" + deployment + '\'' +
                ", nodeselectorterms='" + nodeselectorterms + '\'' +
                ", area='" + area + '\'' +
                ", volumemounts='" + volumemounts + '\'' +
                ", dnspolicy='" + dnspolicy + '\'' +
                ", dnsconfig='" + dnsconfig + '\'' +
                ", tag='" + tag + '\'' +
                ", imagepullsecrets='" + imagepullsecrets + '\'' +
                ", prestop='" + prestop + '\'' +
                ", replicas='" + replicas + '\'' +
                ", cpu_limits='" + cpu_limits + '\'' +
                ", cpu_requests='" + cpu_requests + '\'' +
                ", memory_limits='" + memory_limits + '\'' +
                ", memory_requests='" + memory_requests + '\'' +
                ", volumes='" + volumes + '\'' +
                ", portserver='" + portserver + '\'' +
                ", env='" + env + '\'' +
                ", envfrom='" + envfrom + '\'' +
                ", strategytype='" + strategytype + '\'' +
                ", hostalias='" + hostalias + '\'' +
                ", last_modified_date='" + last_modified_date + '\'' +
                ", allocated_ip_segment='" + allocated_ip_segment + '\'' +
                ", readinessprobe='" + readinessprobe + '\'' +
                ", initContainers='" + initContainers + '\'' +
                '}';
    }
}
