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
@Document(collection = "baseimage_info")
@Data
public class BaseImageEntity extends BaseEntity {
    /**
     * 基础镜像名称（英文镜像名称）
     */
    @Field("name")
    private String name;

    /**
     * 基础镜像名称（中文镜像名称）
     */
    @Field("name_cn")
    private String nameCn;

    /**
     * gitlab项目id
     */
    @Field("gitlab_id")
    private String gitlabId;

}
