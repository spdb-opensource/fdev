package com.spdb.fdev.freport.spdb.entity.component;

import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

/**
 * @Author liux81
 * @DATE 2022/1/25
 */
@EqualsAndHashCode(callSuper = true)
@Component
@Document(collection = "mpass_archetype")
@Data
public class MpassArchetypeEntity extends BaseEntity {

    /**
     * 骨架英文名
     */
    @Field("name_en")
    private String nameEn;
    /**
     * 骨架中文名
     */
    @Field("name_cn")
    private String nameCn;
    /**
     * gitlab项目id
     */
    @Field("gitlab_id")
    private String gitlabId;
}
