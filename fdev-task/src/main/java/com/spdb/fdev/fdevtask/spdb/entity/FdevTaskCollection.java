package com.spdb.fdev.fdevtask.spdb.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;

@Document(collection = "task_collection")
@ApiModel("任务集合")
public class FdevTaskCollection implements Serializable {


    private static final long serialVersionUID = -8761754602413798881L;

    @Id
    @JsonIgnore
    private ObjectId _id;

    @Indexed(unique=true)
    @Field("id")
    @ApiModelProperty(value = "任务集合ID(测试平台JobID)")
    private String id;

    @Indexed(unique=true)
    @Field("task_id")
    @ApiModelProperty(value = "关联任务id")
    private String task_id;

    @Field("taskcollection")
    @ApiModelProperty(value = "子任务集合(id集合)")
    private List<String> taskcollection;


    public FdevTaskCollection() {
    }

    public FdevTaskCollection(String id, String task_id, List<String> taskcollection) {
        this.id = id;
        this.task_id = task_id;
        this.taskcollection = taskcollection;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public List<String> getTaskcollection() {
        return taskcollection;
    }

    public void setTaskcollection(List<String> taskcollection) {
        this.taskcollection = taskcollection;
    }

    public List<String> addToTaskcollection(String taskid) {
        this.taskcollection.add(taskid);
        HashSet<String> set = new HashSet<String>(this.taskcollection);
        this.taskcollection.clear();
        this.taskcollection.addAll(set);
        return this.taskcollection;
    }


    @Override
    public String toString() {
        return "FdevTaskCollection{" +
                "id='" + id + '\'' +
                ", task_id='" + task_id + '\'' +
                ", taskcollection=" + taskcollection +
                '}';
    }
}
