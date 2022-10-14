package com.spdb.fdev.fuser.spdb.entity.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;


/**
 * @Author liux81
 * @DATE 2021/10/26
 */
@Document(collection = "section")
@ApiModel(value="条线")
@Component
public class Section implements Serializable {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    private String id;

    @Field("name_en")
    private String sectionNameEn;

    @Field("name_cn")
    private String sectionNameCn;

    @Field("create_time")
    private String createTime;

    public Section() {
    }

    public Section(String id, String sectionNameEn, String sectionNameCn, String createTime) {
        this.id = id;
        this.sectionNameEn = sectionNameEn;
        this.sectionNameCn = sectionNameCn;
        this.createTime = createTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSectionNameEn() {
        return sectionNameEn;
    }

    public void setSectionNameEn(String sectionNameEn) {
        this.sectionNameEn = sectionNameEn;
    }

    public String getSectionNameCn() {
        return sectionNameCn;
    }

    public void setSectionNameCn(String sectionNameCn) {
        this.sectionNameCn = sectionNameCn;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    //存数据时，初始化_id和id
    public void initId() {
        ObjectId temp = new ObjectId();
        this._id = temp;
        this.id = temp.toString();
    }
}
