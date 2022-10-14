package com.spdb.fdev.release.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = "db_review")
@ApiModel(value="应用与数据库关联信息")
public class DbReview implements Serializable {
	private static final long serialVersionUID = -803155500988474930L;

	@Id
	private ObjectId _id;

	@Field("task_id")
	@ApiModelProperty(value = "任务id")
	private String task_id;

	@Field("release_node_name")
	@ApiModelProperty(value = "投产点名称")
	private String release_node_name;

	@Field("project_id")
	@ApiModelProperty(value = "任务所属应用ID")
	private String project_id;

	@Field("zip_path")
	@ApiModelProperty(value = "任务模块数据库审核zip文件存储路径")
	private List<String> zip_path;

	@Field("minIo_path")
	@ApiModelProperty(value = "投产模块数据库审核sql和sh文件存储路径")
	private List<String> minIo_path;

	@Field("reviewStatus")
	@ApiModelProperty(value = "审核状态")
	private String reviewStatus;       // 0 未通过，1 通过

	@Field("group_abbr")
	@ApiModelProperty(value = "小组标识")
	private String group_abbr;

	@Field("release_date")
	@ApiModelProperty(value = "投产日期")
	private String release_date;

	public String getTask_id() {
		return task_id;
	}

	public void setTask_id(String task_id) {
		this.task_id = task_id;
	}

	public String getRelease_node_name() {
		return release_node_name;
	}

	public void setRelease_node_name(String release_node_name) {
		this.release_node_name = release_node_name;
	}

	public String getProject_id() {
		return project_id;
	}

	public void setProject_id(String project_id) {
		this.project_id = project_id;
	}

	public List<String> getZip_path() {
		return zip_path;
	}

	public void setZip_path(List<String> zip_path) {
		this.zip_path = zip_path;
	}

	public List<String> getMinIo_path() {
		return minIo_path;
	}

	public void setMinIo_path(List<String> minIo_path) {
		this.minIo_path = minIo_path;
	}

	public String getReviewStatus() {
		return reviewStatus;
	}

	public void setReviewStatus(String reviewStatus) {
		this.reviewStatus = reviewStatus;
	}

	public String getGroup_abbr() {
		return group_abbr;
	}

	public void setGroup_abbr(String group_abbr) {
		this.group_abbr = group_abbr;
	}

	public String getRelease_date() {
		return release_date;
	}

	public void setRelease_date(String release_date) {
		this.release_date = release_date;
	}


	public DbReview(String task_id, String release_node_name, String project_id, List<String> zip_path, List<String> minIo_path, String reviewStatus) {
		this.task_id = task_id;
		this.release_node_name = release_node_name;
		this.project_id = project_id;
		this.zip_path = zip_path;
		this.minIo_path = minIo_path;
		this.reviewStatus = reviewStatus;
	}

	public void initId() {
		ObjectId temp = new ObjectId();
		this.task_id=temp.toString();
		this._id=temp;
	}
}
