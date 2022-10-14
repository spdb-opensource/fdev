package com.spdb.fdev.freport.spdb.entity.user;

import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = true)
@Data
public class Role extends BaseEntity {

    @Field("name")
    private String name;

}
