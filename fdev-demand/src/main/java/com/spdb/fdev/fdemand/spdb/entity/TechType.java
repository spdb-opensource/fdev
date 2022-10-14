package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Constants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Constants.TECH_TYPE)
public class TechType {

    @Id
    private ObjectId _id;

    private String id;
    /**
     * 科技类型
     */
    private String techType;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTechType() {
        return techType;
    }

    public void setTechType(String techType) {
        this.techType = techType;
    }
}
