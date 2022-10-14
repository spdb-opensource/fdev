package com.spdb.fdev.freport.spdb.entity.user;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.spdb.entity.SimpleEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@EqualsAndHashCode(callSuper = true)
@Document(collection = EntityDict.AREA)
@Data
public class Area extends SimpleEntity {

    @Field("name")
    private String name;

}
