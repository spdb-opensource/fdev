package com.spdb.fdev.freport.spdb.entity.app;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@EqualsAndHashCode(callSuper = true)
@Component
@Document(collection = EntityDict.APP_ENTITY)
@Data
public class AppEntity extends BaseEntity {

    @Field("gitlab_project_id")
    private Integer gitlabProjectId;

    @Field("name_en")
    private String nameEn;

    @Field("name_zh")
    private String nameZh;

    @Field("desc")
    private String desc;

    @Field("group")
    private String group;

    @Field("git")
    private String git;

    @Field("status")
    private String status;// 哎这个表维护得很差，必须取“非0”才是启用，“0”是废弃

    @Field("createtime")
    private String createTime;

}
