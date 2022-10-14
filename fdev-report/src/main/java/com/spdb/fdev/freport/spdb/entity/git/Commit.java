package com.spdb.fdev.freport.spdb.entity.git;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "commit")
public class Commit {

    @Indexed
    @Field("id")
    private String id;

    @Field("short_id")
    private String shortId;

    @Field("project_id")
    private Integer projectId;

    @Field("project_name")
    private String projectName;

    @Field("committed_date")
    private String committedDate;

    @Field("committer_email")
    private String committerEmail;

    @Field("committer_name")
    private String committerName;

    @Field("stats")
    private Stats stats;

    @Field("web_url")
    private String webUrl;

}
