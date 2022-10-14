package com.spdb.fdev.fdevenvconfig.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Document(collection = "out_side_template")
@ApiModel(value = "外部参数配置模版")
public class OutSideTemplate implements Serializable {

    private static final long serialVersionUID = -5671880097197533657L;

    @Id
    @ApiModelProperty(value = "ID")
    @JsonIgnore
    private ObjectId _id;

    @Field("project_id")
    @ApiModelProperty(value = "应用id")
    private String project_id;

    @Field("variables")
    @ApiModelProperty(value = "外部参数值")
    private List<Map<String, String>> variables;

    @Field("opno")
    @ApiModelProperty(value = "操作员")
    private String opno;

    @Field("ctime")
    @ApiModelProperty(value = "创建时间")
    private String ctime;

    @Field("utime")
    @ApiModelProperty(value = "修改时间")
    private String utime;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getProject_id() {
        return project_id;
    }

    public void setProject_id(String project_id) {
        this.project_id = project_id;
    }

    public List<Map<String, String>> getVariables() {
        return variables;
    }

    public void setVariables(List<Map<String, String>> variables) {
        this.variables = variables;
    }

    public String getOpno() {
        return opno;
    }

    public void setOpno(String opno) {
        this.opno = opno;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUtime() {
        return utime;
    }

    public void setUtime(String utime) {
        this.utime = utime;
    }

    public OutSideTemplate() {
    }

    public OutSideTemplate(ObjectId _id, String project_id, List<Map<String, String>> variables, String opno,
                           String ctime, String utime) {
        super();
        this._id = _id;
        this.project_id = project_id;
        this.variables = variables;
        this.opno = opno;
        this.ctime = ctime;
        this.utime = utime;
    }

    @Override
    public String toString() {
        return "OutSideTemplate [_id=" + _id + ", project_id=" + project_id + ", variables=" + variables + ", opno="
                + opno + ", ctime=" + ctime + ", utime=" + utime + "]";
    }


}
