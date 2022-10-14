package com.spdb.fdev.fuser.spdb.entity.user;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.annotations.ApiModel;

@Document(collection = "UserCommissionEvent")
@ApiModel(value="用户代办事项")
@CompoundIndexes({
    @CompoundIndex(name = "user_commission_idx", def = "{'target_id': 1, 'module': 1, 'type':1}" , unique = true)
})
public class UserCommissionEvent implements Serializable{

	private static final long serialVersionUID = -5779459188855791764L;

	@Id
	private ObjectId _id;
	
	@Field("id")
	private String id; //代办事项id
	
	@Field("user_ids")
	private List<String> user_ids; //代办负责人id集合
	
	@Field("user_list")
	private List<Map> user_list; //代办负责人集合
	
	@Field("target_id")
	private String target_id; //目标id
	
	@Field("module")
	private String module;	//所属模块
	
	@Field("description")
	private String description; //代办描述
	
	@Field("link")
	private String link; //相关代办链接
	
	@Field("status")
	private String status; //代办状态   0是待操作, 1是操作完成
	
	@Field("type")
	private String type; //代办类型
	
	@Field("create_user_id")
	private String create_user_id; //代办事项创建人
	
	@Field("createTime")
	private String createTime; //创建时间
	
	@Field("executor_id")
	private String executor_id; //执行人id
	
	@Field("executor_name_cn")
	private String executor_name_cn; //执行人中文名
	
	@Field("executor_name_en")
	private String executor_name_en; //执行人英文名
	
	@Field("executeTime")
	private String executeTime;	//执行时间
	
	@Field("label")
	private String label;	//代办标签: 值为 todo 或  done
	
	
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
	public List<String> getUser_ids() {
		return user_ids;
	}
	public void setUser_ids(List<String> user_ids) {
		this.user_ids = user_ids;
	}
	public List<Map> getUser_list() {
		return user_list;
	}
	public void setUser_list(List<Map> user_list) {
		this.user_list = user_list;
	}
	public String getTarget_id() {
		return target_id;
	}
	public void setTarget_id(String target_id) {
		this.target_id = target_id;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCreate_user_id() {
		return create_user_id;
	}
	public void setCreate_user_id(String create_user_id) {
		this.create_user_id = create_user_id;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getExecutor_id() {
		return executor_id;
	}
	public void setExecutor_id(String executor_id) {
		this.executor_id = executor_id;
	}
	public String getExecutor_name_cn() {
		return executor_name_cn;
	}
	public void setExecutor_name_cn(String executor_name_cn) {
		this.executor_name_cn = executor_name_cn;
	}
	public String getExecutor_name_en() {
		return executor_name_en;
	}
	public void setExecutor_name_en(String executor_name_en) {
		this.executor_name_en = executor_name_en;
	}
	public String getExecuteTime() {
		return executeTime;
	}
	public void setExecuteTime(String executeTime) {
		this.executeTime = executeTime;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	@Override
	public String toString() {
		return "UserCommissionEvent [id=" + id + ", user_ids=" + user_ids + ", user_list=" + user_list + ", target_id="
				+ target_id + ", module=" + module + ", description=" + description + ", link=" + link + ", status="
				+ status + ", type=" + type + ", create_user_id=" + create_user_id + ", createTime=" + createTime
				+ ", executor_id=" + executor_id + ", executor_name_cn=" + executor_name_cn + ", executor_name_en="
				+ executor_name_en + ", executeTime=" + executeTime + ", label=" + label + "]";
	}
	
	
}
