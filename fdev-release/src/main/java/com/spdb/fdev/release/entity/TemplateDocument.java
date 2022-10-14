package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Document(collection = "template_document")
@ApiModel(value = "模板文件路径表")
public class TemplateDocument implements Serializable {

    private static final long serialVersionUID = -6352078662699404322L;

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @Field("sysname_cn")
    @ApiModelProperty(value = "系统中文名")
    private String sysname_cn;

    @Field("template_type")
    @ApiModelProperty(value = "模板文件类型")
    private String template_type;

    @Field("document_list")
    @ApiModelProperty(value = "存储在minio的文档列表")
    private List<String> document_list;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    @Field("scc_document_list")
    @ApiModelProperty(value="scc模板名称集合")
    private List<String> scc_document_list;

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

    public String getSysname_cn() {
        return sysname_cn;
    }

    public void setSysname_cn(String sysname_cn) {
        this.sysname_cn = sysname_cn;
    }

    public String getTemplate_type() {
        return template_type;
    }

    public void setTemplate_type(String template_type) {
        this.template_type = template_type;
    }

    public List<String> getDocument_list() {
        return document_list;
    }

    public void setDocument_list(List<String> document_list) {
        this.document_list = document_list;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public List<String> getScc_document_list() {
        return scc_document_list;
    }

    public void setScc_document_list(List<String> scc_document_list) {
        this.scc_document_list = scc_document_list;
    }
}
