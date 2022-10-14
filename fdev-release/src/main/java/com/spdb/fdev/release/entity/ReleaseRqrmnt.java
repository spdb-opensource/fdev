package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Component
@Document(collection = "release_rqrmnt")
@ApiModel(value = "投产需求表")
public class ReleaseRqrmnt implements Serializable {

    private static final long serialVersionUID = -192235962477886728L;

    @Id
    @ApiModelProperty(value = "ID")
    private ObjectId _id;

    @Field("id")
    @Indexed(unique = true)
    private String id;

    @Field("release_node_name")
    @NotEmpty(message = "投产窗口名称")
    private String release_node_name;

    @Field("rqrmnt_id")
    private String rqrmnt_id;

    @Field("rqrmnt_num")
    private String rqrmnt_num;

    @Field("rqrmnt_name")
    private String rqrmnt_name;

    @Field("type")
    @NotEmpty(message = "上传类型1:任务上传2:投产上传[投产上传每个投产窗口只有一条]")
    private String type;

    @Field("rqrmnt_file")
    @NotEmpty(message = "需求文件列表")
    private List<Map<String, String>> rqrmnt_file;

    @Field("task_map")
    @NotEmpty(message = "任务集合")
    private Map<String, Object> task_map;

    @Field("create_time")
    private String create_time;

    @Field("update_time")
    @ApiModelProperty(value="修改时间")
    private String update_time;

    private String stage;

    private Object task_list;

    private String operate_time;

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getOperate_time() {
        return operate_time;
    }

    public void setOperate_time(String operate_time) {
        this.operate_time = operate_time;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public ReleaseRqrmnt() {
        super();
    }

    public ReleaseRqrmnt(ObjectId _id, String id,String release_node_name, String rqrmnt_id, String rqrmnt_num,
                         String rqrmnt_name, String type, Map<String, Object> task_map) {
        this._id = _id;
        this.id = id;
        this.release_node_name = release_node_name;
        this.rqrmnt_id = rqrmnt_id;
        this.rqrmnt_num = rqrmnt_num;
        this.rqrmnt_name = rqrmnt_name;
        this.type = type;
        this.task_map = task_map;
    }

    public ReleaseRqrmnt(ObjectId _id, String id,String release_node_name, String rqrmnt_id, String rqrmnt_num,
                         String rqrmnt_name, String type, Map<String, Object> task_map,String create_time) {
        this._id = _id;
        this.id = id;
        this.release_node_name = release_node_name;
        this.rqrmnt_id = rqrmnt_id;
        this.rqrmnt_num = rqrmnt_num;
        this.rqrmnt_name = rqrmnt_name;
        this.type = type;
        this.task_map = task_map;
        this.create_time=create_time;
    }

    public ReleaseRqrmnt(ObjectId _id, String id, String release_node_name, String type,
                         List<Map<String, String>> rqrmnt_file) {
        this._id = _id;
        this.id = id;
        this.release_node_name = release_node_name;
        this.type = type;
        this.rqrmnt_file = rqrmnt_file;
    }

    public ReleaseRqrmnt(ObjectId _id, String id, String release_node_name, String type,
                         List<Map<String, String>> rqrmnt_file,String create_time) {
        this._id = _id;
        this.id = id;
        this.release_node_name = release_node_name;
        this.type = type;
        this.rqrmnt_file = rqrmnt_file;
        this.create_time = create_time;
    }

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

    public String getRelease_node_name() {
        return release_node_name;
    }

    public void setRelease_node_name(String release_node_name) {
        this.release_node_name = release_node_name;
    }

    public String getRqrmnt_id() {
        return rqrmnt_id;
    }

    public void setRqrmnt_id(String rqrmnt_id) {
        this.rqrmnt_id = rqrmnt_id;
    }

    public String getRqrmnt_num() {
        return rqrmnt_num;
    }

    public void setRqrmnt_num(String rqrmnt_num) {
        this.rqrmnt_num = rqrmnt_num;
    }

    public String getRqrmnt_name() {
        return rqrmnt_name;
    }

    public void setRqrmnt_name(String rqrmnt_name) {
        this.rqrmnt_name = rqrmnt_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Map<String, String>> getRqrmnt_file() {
        return rqrmnt_file;
    }

    public void setRqrmnt_file(List<Map<String, String>> rqrmnt_file) {
        this.rqrmnt_file = rqrmnt_file;
    }

    public Map<String, Object> getTask_map() {
        return task_map;
    }

    public void setTask_map(Map<String, Object> task_map) {
        this.task_map = task_map;
    }

    public Object getTask_list() {
        return task_list;
    }

    public void setTask_list(Object task_list) {
        this.task_list = task_list;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }
}
