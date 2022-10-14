package com.spdb.fdev.pipeline.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

// yaml参数的 插件 用到的entity
@Component
public class YamlConfigEntity {

    @Field("entityId")
    private String entityId;

    @Field("entityEn")
    private String entityEn;

    @Field("entityCn")
    private String entityCn;

    @Override
    public String toString() {
        return "{" +
                "entityId=" + entityId +
                ", entityEn=" + entityEn +
                ", entityCn=" + entityCn +
                '}';
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityEn() {
        return entityEn;
    }

    public void setEntityEn(String entityEn) {
        this.entityEn = entityEn;
    }

    public String getEntityCn() {
        return entityCn;
    }

    public void setEntityCn(String entityCn) {
        this.entityCn = entityCn;
    }
}
