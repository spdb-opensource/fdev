package com.spdb.fdev.fdevtask.spdb.entity;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@Document(collection = "app_deploy_review")
@ApiModel(value = "应用部署复核")
public class AppDeployReview implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4249249417082431856L;

	@Id
    @JsonIgnore
    private ObjectId _id;

    @Field("id")
    @ApiModelProperty(value = "申请部署ID")
    @Indexed(unique = true)
    private String id;
    
    @Field("task_id")
    @ApiModelProperty(value = "任务Id")
    private String taskId;
    
    @Field("task_name")
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    
    @Field("demand_id")
    @ApiModelProperty(value = "需求Id")
    private String demandId;
    
    @Field("oa_contact_no")
    @ApiModelProperty(value = "需求编号")
    private String oaContactNo;
    
    @Field("oa_contact_name")
    @ApiModelProperty(value = "需求名称")
    private String oaContactName;

    @Field("application_id")
    @ApiModelProperty(value = "应用Id")
    private String applicationId;
    
    @Field("app_git_url")
    @ApiModelProperty(value = "应用git地址")
    private String appGitUrl;
    
    @Field("git_pro_id")
    @ApiModelProperty(value = "应用gitId")
    private Integer gitProId;
    
    @Field("application_name_en")
    @ApiModelProperty(value = "应用英文名称")
    private String applicationNameEn;
    
    @Field("application_name_zh")
    @ApiModelProperty(value = "应用中文名称")
    private String applicationNameZh;

    @Field("deploy_env")
    @ApiModelProperty(value = "部署环境")
    private String deployEnv;
    
    @Field("app_branch")
    @ApiModelProperty(value = "分支")
    private String appBranch;
    
    @Field("applay_user_id")
    @ApiModelProperty(value = "申请人用户id")
    private String applayUserId;
    
    @Field("applay_user_name_zh")
    @ApiModelProperty(value = "申请人中文名称")
    private String applayUserNameZh;
    
    @Field("overdue_reason")
    @ApiModelProperty(value = "申请原因")
    private String overdueReason;
    
    @Field("merge_reason")
    @ApiModelProperty(value = "申请原因En")
    private String mergeReason;
    
    @Field("applay_user_owner_group_id")
    @ApiModelProperty(value = "申请人所属小组id")
    private String applayUserOwnerGroupId;
    
    @Field("applay_user_owner_group")
    @ApiModelProperty(value = "申请人所属小组")
    private String applayUserOwnerGroup;
    
    @Field("applay_date")
    @ApiModelProperty(value = "申请日期")
    private String applayDate;
    
	@Field("applay_time")
    @ApiModelProperty(value = "申请时间")
    private String applayTime;
    
    @Field("review_user_id")
    @ApiModelProperty(value = "审核人id")
    private String reviewUserId;
    
    @Field("review_user_name_zh")
    @ApiModelProperty(value = "审核人中文名称")
    private String reviewUserNameZh;
    
    @Field("review_status") 
    @ApiModelProperty(value = "审核状态") // 0:未审核 1:已审核
    private String reviewStatus;
    
    @Field("review_time")
    @ApiModelProperty(value = "审核时间")
    private String reviewTime;
    
    @Field("deploy_status") 
    @ApiModelProperty(value = "部署状态")//0:未部署 1:部署失败 2:部署成功 3:部署中
    private String deploy_status;
    
    @Field("image_caas_url")
    @ApiModelProperty(value = "caas镜像地址")
    private String imageCaasUrl;
    
    @Field("image_scc_url")
    @ApiModelProperty(value = "scc镜像地址")
    private String imageSccUrl;
    
    @Field("image_push_status")
    @ApiModelProperty(value = "镜像推送状态")//0：已推送caas 1：已推送scc 
    private String imagePushStatus;
    
    @Field("push_time")
    @ApiModelProperty(value = "推送时间")
    private String pushTime;

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

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getDemandId() {
		return demandId;
	}

	public void setDemandId(String demandId) {
		this.demandId = demandId;
	}

	public String getOaContactNo() {
		return oaContactNo;
	}

	public void setOaContactNo(String oaContactNo) {
		this.oaContactNo = oaContactNo;
	}

	public String getOaContactName() {
		return oaContactName;
	}

	public void setOaContactName(String oaContactName) {
		this.oaContactName = oaContactName;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	
	public String getApplicationNameEn() {
		return applicationNameEn;
	}

	public void setApplicationNameEn(String applicationNameEn) {
		this.applicationNameEn = applicationNameEn;
	}

	public String getApplicationNameZh() {
		return applicationNameZh;
	}

	public void setApplicationNameZh(String applicationNameZh) {
		this.applicationNameZh = applicationNameZh;
	}

	public String getDeployEnv() {
		return deployEnv;
	}

	public void setDeployEnv(String deployEnv) {
		this.deployEnv = deployEnv;
	}
	
	public String getAppGitUrl() {
		return appGitUrl;
	}

	public void setAppGitUrl(String appGitUrl) {
		this.appGitUrl = appGitUrl;
	}

	public String getApplayUserId() {
		return applayUserId;
	}

	public void setApplayUserId(String applayUserId) {
		this.applayUserId = applayUserId;
	}

	public String getApplayUserNameZh() {
		return applayUserNameZh;
	}

	public void setApplayUserNameZh(String applayUserNameZh) {
		this.applayUserNameZh = applayUserNameZh;
	}

	public String getOverdueReason() {
		return overdueReason;
	}

	public void setOverdueReason(String overdueReason) {
		this.overdueReason = overdueReason;
	}
	
	public String getMergeReason() {
		return mergeReason;
	}

	public void setMergeReason(String mergeReason) {
		this.mergeReason = mergeReason;
	}

	public String getApplayUserOwnerGroupId() {
		return applayUserOwnerGroupId;
	}

	public void setApplayUserOwnerGroupId(String applayUserOwnerGroupId) {
		this.applayUserOwnerGroupId = applayUserOwnerGroupId;
	}

	public String getApplayUserOwnerGroup() {
		return applayUserOwnerGroup;
	}

	public void setApplayUserOwnerGroup(String applayUserOwnerGroup) {
		this.applayUserOwnerGroup = applayUserOwnerGroup;
	}
	
	public String getApplayDate() {
		return applayDate;
	}

	public void setApplayDate(String applayDate) {
		this.applayDate = applayDate;
	}

	public String getApplayTime() {
		return applayTime;
	}

	public void setApplayTime(String applayTime) {
		this.applayTime = applayTime;
	}

	public String getReviewUserId() {
		return reviewUserId;
	}

	public void setReviewUserId(String reviewUserId) {
		this.reviewUserId = reviewUserId;
	}

	public String getReviewUserNameZh() {
		return reviewUserNameZh;
	}

	public void setReviewUserNameZh(String reviewUserNameZh) {
		this.reviewUserNameZh = reviewUserNameZh;
	}

	public String getReviewStatus() {
		return reviewStatus;
	}

	public void setReviewStatus(String reviewStatus) {
		this.reviewStatus = reviewStatus;
	}

	public String getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}

	public String getDeploy_status() {
		return deploy_status;
	}

	public void setDeploy_status(String deploy_status) {
		this.deploy_status = deploy_status;
	}

	public String getImageCaasUrl() {
		return imageCaasUrl;
	}

	public void setImageCaasUrl(String imageCaasUrl) {
		this.imageCaasUrl = imageCaasUrl;
	}

	public String getImageSccUrl() {
		return imageSccUrl;
	}

	public void setImageSccUrl(String imageSccUrl) {
		this.imageSccUrl = imageSccUrl;
	}

	public String getImagePushStatus() {
		return imagePushStatus;
	}

	public void setImagePushStatus(String imagePushStatus) {
		this.imagePushStatus = imagePushStatus;
	}
	
	public String getAppBranch() {
		return appBranch;
	}

	public void setAppBranch(String appBranch) {
		this.appBranch = appBranch;
	}

	public Integer getGitProId() {
		return gitProId;
	}

	public void setGitProId(Integer gitProId) {
		this.gitProId = gitProId;
	}

	public String getPushTime() {
		return pushTime;
	}

	public void setPushTime(String pushTime) {
		this.pushTime = pushTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
    
}
