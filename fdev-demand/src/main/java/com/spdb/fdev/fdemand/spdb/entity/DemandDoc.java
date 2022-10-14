package com.spdb.fdev.fdemand.spdb.entity;

import com.spdb.fdev.fdemand.base.dict.Dict;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.stereotype.Component;

@Component
@Document(collection = Dict.DEMAND_DOC)
public class DemandDoc {

    @Id
    private ObjectId _id;
    @Field("id")
    private String id;

    /**
     * 需求id，关联需求表
     */
    @Field("demand_id")
    public String demand_id;

    /**
     * 文件名称
     */
    @Field("doc_name")
    public String doc_name;

    /**
     * 文件类型
     */
    @Field("doc_type")
    public String doc_type;

    /**
     * 文件路径
     */
    @Field("doc_path")
    public String doc_path;

    /**
     * 文档链接
     */
    @Field("doc_link")
    public String doc_link;

    /**
     * 上传用户
     */
    @Field("upload_user")
    public String upload_user;
    
    /**
     * 上传用户信息
     */
    @Field("upload_user_all")
    private UserInfo upload_user_all;

    /**
     * 用户所属小组
     */
    @Field("user_group_id")
    public String user_group_id;

    /**
     * 用户所属小组
     */
    @Field("user_group_cn")
    public String user_group_cn;
    
    /**
     * 上传时间
     */
    @Field("create_time")
    public String create_time;
    
    /**
     * 更新时间
     */
    @Field("update_time")
    public String update_time;


    /**
     * 需求种类，一般需求文档库是demand，需求评估文档库是demandAccess
     */
    @Field("demand_kind")
    public String demand_kind;
    

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

    public String getDemand_id() {
        return demand_id;
    }

    public void setDemand_id(String demand_id) {
        this.demand_id = demand_id;
    }

    public String getDoc_name() {
        return doc_name;
    }

    public void setDoc_name(String doc_name) {
        this.doc_name = doc_name;
    }

    public String getDoc_type() {
        return doc_type;
    }

    public void setDoc_type(String doc_type) {
        this.doc_type = doc_type;
    }

    public String getDoc_path() {
        return doc_path;
    }

    public void setDoc_path(String doc_path) {
        this.doc_path = doc_path;
    }

    public String getUpload_user() {
        return upload_user;
    }

    public void setUpload_user(String upload_user) {
        this.upload_user = upload_user;
    }

    public String getUser_group_id() {
        return user_group_id;
    }

    public void setUser_group_id(String user_group_id) {
        this.user_group_id = user_group_id;
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

	public UserInfo getUpload_user_all() {
		return upload_user_all;
	}

	public void setUpload_user_all(UserInfo upload_user_all) {
		this.upload_user_all = upload_user_all;
	}

	public String getUser_group_cn() {
		return user_group_cn;
	}

	public void setUser_group_cn(String user_group_cn) {
		this.user_group_cn = user_group_cn;
	}

    public String getDoc_link() {
        return doc_link;
    }

    public void setDoc_link(String doc_link) {
        this.doc_link = doc_link;
    }

    public String getDemand_kind() {
        return demand_kind;
    }

    public void setDemand_kind(String demand_kind) {
        this.demand_kind = demand_kind;
    }
}
