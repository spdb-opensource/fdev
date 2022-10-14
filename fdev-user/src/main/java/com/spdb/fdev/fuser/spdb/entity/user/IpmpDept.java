package com.spdb.fdev.fuser.spdb.entity.user;

import com.spdb.fdev.fuser.base.dict.Constants;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Constants.IPMP_DEPT)
public class IpmpDept {
    @Id
    private ObjectId _id;
    /**
     * 机构id
     */
    @Field("dept_id")
    @Indexed
    private String dept_id;
    /**
     * 机构名称
     */
    @Field("dept_name")
    private String dept_name;
    /**
     * 上级机构id
     */
    @Field("parent_id")
    private String parent_id;
    /**
     * 机构状态
     */
    @Field("dept_status")
    private String dept_status;

    /**
     * 创建时间
     */
    @Field("create_time")
    private String create_time;

    /**
     * 修改时间
     */
    @Field("update_time")
    private String update_time;

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public String getDept_id() {
        return dept_id;
    }

    public void setDept_id(String dept_id) {
        this.dept_id = dept_id;
    }

    public String getDept_name() {
        return dept_name;
    }

    public void setDept_name(String dept_name) {
        this.dept_name = dept_name;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public String getDept_status() {
        return dept_status;
    }

    public void setDept_status(String dept_status) {
        this.dept_status = dept_status;
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
