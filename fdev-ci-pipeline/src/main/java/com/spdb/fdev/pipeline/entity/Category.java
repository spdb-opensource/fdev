package com.spdb.fdev.pipeline.entity;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Map;

//分类实体
@Document("category")
@Component
public class Category {

    @Id
    @Field("_id")
    private ObjectId _id;

    @Field("categoryId")
    private String categoryId;

    @Field("categoryLevel")
    private String categoryLevel;       //插件级别，即代表了是那个类型的插件

    @Field("categoryName")
    private String categoryName;

    @Field("parentId")
    private String parentId;            //父级id

    @Field("categoryNameEn")
    private String categoryNameEn;      //分类英文名


    public Category(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }


    public Category() {
    }

    public Category(ObjectId _id, String categoryId, String categoryLevel, String categoryName, String parentId) {
        this._id = _id;
        this.categoryId = categoryId;
        this.categoryLevel = categoryLevel;
        this.categoryName = categoryName;
        this.parentId = parentId;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryLevel() {
        return categoryLevel;
    }

    public void setCategoryLevel(String categoryLevel) {
        this.categoryLevel = categoryLevel;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getCategoryNameEn() {
        return categoryNameEn;
    }

    public void setCategoryNameEn(String categoryNameEn) {
        this.categoryNameEn = categoryNameEn;
    }
}
