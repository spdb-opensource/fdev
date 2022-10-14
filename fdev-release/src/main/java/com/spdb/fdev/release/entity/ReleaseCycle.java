package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Document(collection = "release_cycle")
@ApiModel(value = "投产模块周期表")
public class ReleaseCycle implements Serializable {
    private static final long serialVersionUID = 3605415491533224681L;

    @Field("release_node_name")
    @NotEmpty(message = "投产大窗口名称不能为空")
    @Indexed(unique = true)
    @ApiModelProperty(value = "投产大窗口名称")
    private String release_node_name;

    @Field("T")
    @NotEmpty(message = "T的日期不能为空")
    @ApiModelProperty(value = "T")
    private String t;

    @Field("T-1")
    @NotEmpty(message = "T-1的日期不能为空")
    @ApiModelProperty(value = "T-1")
    private String t_1;

    @Field("T-2")
    @NotEmpty(message = "T-2的日期不能为空")
    @ApiModelProperty(value = "T-2")
    private String t_2;

    @Field("T-3")
    @NotEmpty(message = "T-3的日期不能为空")
    @ApiModelProperty(value = "T-3")
    private String t_3;

    @Field("T-4")
    @NotEmpty(message = "T-4的日期不能为空")
    @ApiModelProperty(value = "T-4")
    private String t_4;

    @Field("T-5")
    @NotEmpty(message = "T-5的日期不能为空")
    @ApiModelProperty(value = "T-5")
    private String t_5;

    @Field("T-6")
    @NotEmpty(message = "T-6的日期不能为空")
    @ApiModelProperty(value = "T-6")
    private String t_6;

    @Field("T-7")
    @NotEmpty(message = "T-7的日期不能为空")
    @ApiModelProperty(value = "T-7")
    private String t_7;

    @Field("T-8")
    @NotEmpty(message = "T-7的日期不能为空")
    @ApiModelProperty(value = "T-8")
    private String t_8;

    @Field("create_time")
    @ApiModelProperty(value="创建时间")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }

    public String getT_1() {
        return t_1;
    }

    public void setT_1(String t_1) {
        this.t_1 = t_1;
    }

    public String getT_2() {
        return t_2;
    }

    public void setT_2(String t_2) {
        this.t_2 = t_2;
    }

    public String getT_3() {
        return t_3;
    }

    public void setT_3(String t_3) {
        this.t_3 = t_3;
    }

    public String getT_4() {
        return t_4;
    }

    public void setT_4(String t_4) {
        this.t_4 = t_4;
    }

    public String getT_5() {
        return t_5;
    }

    public void setT_5(String t_5) {
        this.t_5 = t_5;
    }

    public String getT_6() {
        return t_6;
    }

    public void setT_6(String t_6) {
        this.t_6 = t_6;
    }

    public String getT_7() {
        return t_7;
    }

    public void setT_7(String t_7) {
        this.t_7 = t_7;
    }

    public String getT_8() {
        return t_8;
    }

    public void setT_8(String t_8) {
        this.t_8 = t_8;
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
}
