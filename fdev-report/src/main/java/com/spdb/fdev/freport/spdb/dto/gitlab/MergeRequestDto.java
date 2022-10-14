package com.spdb.fdev.freport.spdb.dto.gitlab;

import lombok.Data;

@Data
public class MergeRequestDto {

    private String id;//合并id

    private String project_id;

    private String title;//合并标题

    private String description;//合并描述

    private String source_branch;

    private String target_branch;

    private Author author;

    @Data
    public class Author {
        String id;
        String name;
    }

}
