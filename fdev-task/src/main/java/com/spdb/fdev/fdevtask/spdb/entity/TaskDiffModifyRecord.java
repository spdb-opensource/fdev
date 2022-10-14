package com.spdb.fdev.fdevtask.spdb.entity;

import io.swagger.annotations.ApiModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

@Document("taskDiff_modify_record")
@ApiModel("任务难度修改记录")
public class TaskDiffModifyRecord {
    @Field("id")
    private String id;
    @Id
    private Object _id;
    @Field("modifyRecord")
    private List<Map<String,Object>> modifyRecord;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object get_id() {
        return _id;
    }

    public void set_id(Object _id) {
        this._id = _id;
    }

    public List<Map<String, Object>> getModifyRecord() {
        return modifyRecord;
    }

    public void setModifyRecord(List<Map<String, Object>> modifyRecord) {
        this.modifyRecord = modifyRecord;
    }
}
