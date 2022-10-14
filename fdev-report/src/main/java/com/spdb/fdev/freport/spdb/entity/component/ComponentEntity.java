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
@Document(collection = "component_info")
@Data
public class ComponentEntity extends BaseEntity {

    @Field("name_en")
    private String nameEn;

    @Field("name_cn")
    private String nameCn;

    @Field("gitlab_id")
    private String gitlabId;

}
