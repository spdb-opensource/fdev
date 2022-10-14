package com.spdb.fdev.freport.spdb.entity.report;

import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@ApiModel(value = "gitlab合并记录")
@Document(collection = "gitlab_merge_record")
@EqualsAndHashCode(callSuper = true)
@Data
public class GitlabMergeRecord extends BaseEntity {

    @Field("merge_id")
    @Indexed(unique = true)
    @ApiModelProperty(value = "合并id")
    private Integer mergeId;

    @Field("group_id")
    @ApiModelProperty(value = "组id")
    private String groupId;

    @Field("project_id")
    @ApiModelProperty(value = "项目id")
    private Integer projectId;

    @Field("repository_name")
    @ApiModelProperty(value = "仓库名称")
    private String repositoryName;

    @Field("title")
    @ApiModelProperty(value = "标题")
    private String title;

    @Field("description")
    @ApiModelProperty(value = "描述")
    private String description;

    @Field("source_branch")
    @ApiModelProperty(value = "源分支")
    private String sourceBranch;

    @Field("target_branch")
    @ApiModelProperty(value = "目标分支")
    private String targetBranch;

    @Field("creator_gitlab_id")
    @ApiModelProperty(value = "gitlab创建人ID")
    private Integer creatorGitlabId;

    @Field("creator_gitlab_name")
    @ApiModelProperty(value = "创建人gitlab名称")
    private String creatorGitlabName;

}
