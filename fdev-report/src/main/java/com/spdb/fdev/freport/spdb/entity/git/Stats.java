package com.spdb.fdev.freport.spdb.entity.git;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Data
public class Stats {

    @Field("total")
    private Integer total;

    @Field("additions")
    private Integer additions;

    @Field("deletions")
    private Integer deletions;

}
