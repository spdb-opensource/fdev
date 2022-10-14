package com.spdb.fdev.fdevinterface.spdb.entity;

import com.spdb.fdev.fdevinterface.base.dict.Dict;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

@Document(collection = Dict.INTERFACE_APPROVE_STATUS)
public class ApproveStatus implements Serializable {

    private static final long serialVersionUID = 8294066886615502223L;

    @Id
    private String id;

    // 提供方应用id
    @Field(Dict.APPID)
    private String appId;

    // 调用方方应用id
    @Field(Dict.CALLINGID)
    private String callingId;

    // 交易码
    @Field(Dict.TRANS_ID)
    private String transId;

    // 调用方
    @Field(Dict.SERVICE_CALLING)
    private String serviceCalling;

    // 服务方
    @Field(Dict.SERVICE_ID)
    private String serviceId;

    // 调用方应用负责人
    @Field(Dict.CALLING_MANAGERS)
    private List<String> callingManagers;

    // 调用方行内应用负责人
    @Field(Dict.CALLING_SPDB_MANAGERS)
    private List<String> callingSpdbManagers;

    // 提供方应用负责人
    @Field(Dict.PROVIDE_MANAGERS)
    private List<String> provideManagers;

    // 提供方行内应用负责人
    @Field(Dict.PROVIDE_SPDB_MANAGERS)
    private List<String> provideSpdbManagers;

    // 申请人
    @Field(Dict.APPLICANT)
    private String applicant;

    // 审批人
    @Field(Dict.APPROVER)
    private String approver;

    // 申请理由
    @Field(Dict.REASON)
    private String reason;

    // 拒绝审批理由
    @Field(Dict.REFUSEREASON)
    private String refuseReason;

    // 审批状态
    @Field(Dict.STATUS)
    private String status;

    @Field(Dict.CREATE_TIME)
    private String createTime;

    @Field(Dict.UPDATE_TIME)
    private String updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransId() {
        return transId;
    }

    public void setTransId(String transId) {
        this.transId = transId;
    }

    public String getServiceCalling() {
        return serviceCalling;
    }

    public void setServiceCalling(String serviceCalling) {
        this.serviceCalling = serviceCalling;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<String> getCallingManagers() {
		return callingManagers;
	}

	public void setCallingManagers(List<String> callingManagers) {
		this.callingManagers = callingManagers;
	}

	public List<String> getCallingSpdbManagers() {
		return callingSpdbManagers;
	}

	public void setCallingSpdbManagers(List<String> callingSpdbManagers) {
		this.callingSpdbManagers = callingSpdbManagers;
	}

	public List<String> getProvideManagers() {
		return provideManagers;
	}

	public void setProvideManagers(List<String> provideManagers) {
		this.provideManagers = provideManagers;
	}

	public List<String> getProvideSpdbManagers() {
		return provideSpdbManagers;
	}

	public void setProvideSpdbManagers(List<String> provideSpdbManagers) {
		this.provideSpdbManagers = provideSpdbManagers;
	}

	public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRefuseReason() {
        return refuseReason;
    }

    public void setRefuseReason(String refuseReason) {
        this.refuseReason = refuseReason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getCallingId() {
        return callingId;
    }

    public void setCallingId(String callingId) {
        this.callingId = callingId;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }
}
