package com.spdb.fdev.freport.spdb.entity.sonar;

import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import com.spdb.fdev.freport.spdb.vo.Ranking;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Document(collection = "sonar_project_rank")
@ApiModel(value = "sonar扫描分支排行表(简单方案 - 直接缓存计算结果)")
@EqualsAndHashCode(callSuper = true)
@Data
public class SonarProjectRank extends BaseEntity {

    @Field("code")
    @Indexed(unique = true)
    private String code;

    @Field("data")
    private List<Ranking> data;

}
