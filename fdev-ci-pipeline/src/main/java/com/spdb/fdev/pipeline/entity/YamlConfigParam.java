package com.spdb.fdev.pipeline.entity;

import com.spdb.fdev.base.utils.CommonUtils;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;

// yaml类型的params
@Component
public class YamlConfigParam {

    @Field("entityTemplateId")
    private String entityTemplateId;

    @Field("entityTemplateEn")
    private String entityTemplateEn;

    @Field("entityTemplateCn")
    private String entityTemplateCn;

    @Field("hint")
    private String hint;                 //对实体模板的描述提示

    @Field("required")
    private Boolean required;           //实体模板下的实体是否必选

    @Field("chooseMore")
    private Boolean chooseMore;         //实体模板下的实体是否可以多选

    @Field("entity")
    private List<YamlConfigEntity> entity;      //实体参数， 当为public的模版时候 ， 该值为空

    public String getEntityTemplateId() {
        return entityTemplateId;
    }

    public void setEntityTemplateId(String entityTemplateId) {
        this.entityTemplateId = entityTemplateId;
    }

    public String getEntityTemplateEn() {
        return entityTemplateEn;
    }

    public void setEntityTemplateEn(String entityTemplateEn) {
        this.entityTemplateEn = entityTemplateEn;
    }

    public String getEntityTemplateCn() {
        return entityTemplateCn;
    }

    public void setEntityTemplateCn(String entityTemplateCn) {
        this.entityTemplateCn = entityTemplateCn;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getChooseMore() {
        return chooseMore;
    }

    public void setChooseMore(Boolean chooseMore) {
        this.chooseMore = chooseMore;
    }

    public List<YamlConfigEntity> getEntity() {
        return entity;
    }

    public void setEntity(List<YamlConfigEntity> entity) {
        this.entity = entity;
    }

    @Override
    public String toString() {
        return "{" +
                "entityTemplateId=" + entityTemplateId +
                ", entityTemplateEn=" + entityTemplateEn +
                ", entityTemplateCn=" + entityTemplateCn +
                ", hint=" + hint +
                ", required=" + required +
                ", chooseMore=" + chooseMore +
                ", entity=" + entity.toString() +
                '}';
    }

}
