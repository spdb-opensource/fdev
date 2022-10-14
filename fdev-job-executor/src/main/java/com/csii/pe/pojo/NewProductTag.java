package com.csii.pe.pojo;


import com.csii.pe.spdb.common.util.CommonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Document(collection = "new_product_tag")
@CompoundIndexes({
        @CompoundIndex(name = "gitlab_index", def = "{'gitlab_project_id': 1, 'product_tag': 1}", unique = true)
})
public class NewProductTag {

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Field("gitlab_project_id")
    private Integer gitlab_project_id;

    @Field("product_tag")
    private String product_tag;

    @Field("already_clean")
    private Integer already_clean;

    @Field("create_time")
    private String create_time;

    public NewProductTag() {

    }

    public NewProductTag(Integer gitlab_project_id, String product_tag, Integer already_clean) {
        this.gitlab_project_id = gitlab_project_id;
        this.product_tag = product_tag;
        this.already_clean = already_clean;
        this.create_time = CommonUtils.sdf.format(new Date());
    }

    public Integer getGitlab_project_id() {
        return gitlab_project_id;
    }

    public void setGitlab_project_id(Integer gitlab_project_id) {
        this.gitlab_project_id = gitlab_project_id;
    }

    public String getProduct_tag() {
        return product_tag;
    }

    public void setProduct_tag(String product_tag) {
        this.product_tag = product_tag;
    }

    public Integer getAlready_clean() {
        return already_clean;
    }

    public void setAlready_clean(Integer already_clean) {
        this.already_clean = already_clean;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    @Override
    public String toString() {
        return "ProductTag{" +
                "gitlab_project_id=" + gitlab_project_id +
                ", product_tag='" + product_tag + '\'' +
                ", already_clean=" + already_clean +
                ", create_time='" + create_time + '\'' +
                '}';
    }
}
