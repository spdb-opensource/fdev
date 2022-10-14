package com.spdb.fdev.freport.spdb.dto.gitlab;

import lombok.Data;

@Data
public class ObjectAttributes {

    private Integer author_id;//创建者id

    private String created_at;//创建时间 09-13 03:40:57 UTC

    private String description;//描述

    private Integer id;//id

    private String iid;//iid

    private String merge_status;//合并状态 unchecked

    private String merge_user_id;//合并人id

    private String source_branch;//源分支

    private String target_branch;//目标分支

    private String title;//标题
}
