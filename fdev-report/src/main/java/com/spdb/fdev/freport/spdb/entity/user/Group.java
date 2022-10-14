package com.spdb.fdev.freport.spdb.entity.user;

import com.spdb.fdev.freport.base.dict.EntityDict;
import com.spdb.fdev.freport.spdb.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Component
@Document(collection = EntityDict.GROUP)
@Data
public class Group extends BaseEntity {

    @Field("name")
    private String name;

    @Field("count")
    private Integer count;

    @Field("parent_id")
    private String parentId;

    @Field("status")
    private String status;

    @Field("sortNum")
    private String sortNum;

    private List<Group> children;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return getId().equals(group.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
