package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author dafeng
 * @date 2021/2/23 14:58
 */
@Component
@ApiModel(value = "选择实体模版、实体、手输值、环境")
public class EntityTemplateParam {

    @Field("id")
    @ApiModelProperty(value = "实体模板ID")
    private String id;

    @Field("nameEn")
    @ApiModelProperty(value = "实体模板英文名")
    private String nameEn;

    @Field("nameCn")
    @ApiModelProperty(value = "实体模板中文名")
    private String nameCn;

    @Field("entity")
    @ApiModelProperty(value = "实体")
    private Entity entity;

    @Field("entityParams")
    @ApiModelProperty(value = "手动输入的参数")
    private List<Map<String,Object>> entityParams;

    @Field("env")
    @ApiModelProperty(value = "环境")
    private EnvInfo env;

    public EntityTemplateParam(String id, String nameEn, String nameCn) {
        this.id = id;
        this.nameEn = nameEn;
        this.nameCn = nameCn;
    }

    public EntityTemplateParam() {
    }

    public EntityTemplateParam(Entity entity) {
        this.entity = entity;
    }



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameCn() {
        return nameCn;
    }

    public void setNameCn(String nameCn) {
        this.nameCn = nameCn;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public List<Map<String, Object>> getEntityParams() {
        return entityParams;
    }

    public void setEntityParams(List<Map<String, Object>> entityParams) {
        this.entityParams = entityParams;
    }

    public EnvInfo getEnv() {
        return env;
    }

    public void setEnv(EnvInfo env) {
        this.env = env;
    }
}
