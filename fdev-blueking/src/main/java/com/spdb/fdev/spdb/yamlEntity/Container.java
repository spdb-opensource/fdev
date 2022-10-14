package com.spdb.fdev.spdb.yamlEntity;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author:guanz2
 * @Date:2021/10/2-21:49
 * @Description: 映射caas生成yaml中的container部分，简化代码。
 */
public class Container {

    @JSONField(ordinal = 1)
    private String name;

    @JSONField(ordinal = 2)
    private List envFrom;

    @JSONField(ordinal = 3)
    private List env;

    @JSONField(ordinal = 4)
    private String image;

    @JSONField(ordinal = 5)
    private String imagePullPolicy;

    @JSONField(ordinal = 6)
    private Map lifecycle;

    @JSONField(ordinal = 7)
    private List ports;

    @JSONField(ordinal = 8)
    private Map resources;

    @JSONField(ordinal = 9)
    private List volumeMounts;

    @JSONField(ordinal = 10)
    private Map readinessProbe;

    public Container() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getEnv() {
        return env;
    }

    public void setEnv(List env) { this.env = env; }

    public List getEnvFrom() {
        return envFrom;
    }

    public void setEnvFrom(List envFrom) { this.envFrom = envFrom; }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImagePullPolicy() {
        return imagePullPolicy;
    }

    public void setImagePullPolicy(String imagePullPolicy) {
        this.imagePullPolicy = imagePullPolicy;
    }

    public Map getLifecycle() {
        return lifecycle;
    }

    public void setLifecycle(Map prepstop) {
        if(prepstop == null){
            this.lifecycle = null;
        }else{
            Map temp = new LinkedHashMap();
            temp.put("preStop", prepstop);
            this.lifecycle = temp;
        }
    }

    public List getPorts() {
        return ports;
    }

    public void setPorts(List ports) { this.ports = ports; }

    public Map getResources() {
        return resources;
    }

    public void setResources(String cpu_limits, String memory_limits, String cpu_requests, String memory_requests) {
        Map limits = new LinkedHashMap();
        limits.put("cpu",cpu_limits);
        limits.put("memory", memory_limits);
        Map requests = new LinkedHashMap();
        requests.put("cpu", cpu_requests);
        requests.put("memory",memory_requests);
        Map temp = new LinkedHashMap();
        temp.put("limits", limits);
        temp.put("requests", requests);
        this.resources = temp;
    }

    public List getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(List volumeMounts) { this.volumeMounts = volumeMounts; }

    public Map getReadinessProbe() {
        return readinessProbe;
    }

    public void setReadinessProbe(Map readinessProbe) {
        this.readinessProbe = readinessProbe;
    }

    @Override
    public String toString() {
        return "Container{" +
                "name='" + name + '\'' +
                ", envFrom=" + envFrom +
                ", env=" + env +
                ", image='" + image + '\'' +
                ", imagePullPolicy='" + imagePullPolicy + '\'' +
                ", lifecycle=" + lifecycle +
                ", ports=" + ports +
                ", resources=" + resources +
                ", volumeMounts=" + volumeMounts +
                ", readinessProbe=" + readinessProbe +
                '}';
    }
}
