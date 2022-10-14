package com.spdb.fdev.pipeline.entity;

import io.swagger.annotations.ApiModel;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author dafeng
 * @date 2021/2/3 19:03
 */
@Component
@ApiModel(value = "选择实体模版、实体、输入方式、手输值")
public class TemplateParamInfo {


    @Field("entity")
    private Entity entity;            //实体

    @Field("entityTemplate")
    private Entity entityTemplate	;   //实体模板

    @Field("manualInputFlag")
    private Boolean manualInputFlag;        //手动输入为true

    /**
     * 新增的手动录入变量用来接收选择手动输入时  输入的参数
     */
    @Field("manualInput")
    private List<Map<String,String>> manualInput;

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntityTemplate() {
        return entityTemplate;
    }

    public void setEntityTemplate(Entity entityTemplate) {
        this.entityTemplate = entityTemplate;
    }

    public Boolean getManualInputFlag() {
        return manualInputFlag;
    }

    public void setManualInputFlag(Boolean manualInputFlag) {
        this.manualInputFlag = manualInputFlag;
    }

    public List<Map<String, String>> getManualInput() {
        return manualInput;
    }

    public void setManualInput(List<Map<String, String>> manualInput) {
        this.manualInput = manualInput;
    }
}
