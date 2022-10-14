package com.spdb.fdev.gitlabwork.entiy;

import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * GitlabWork
 *
 * @blame Android Team
 */
@Document(collection = "gitlabWorkInfo")
public class GitlabWork implements Serializable {
    private String userId;

    @Field("userName")
    @ApiModelProperty(value="人员git用户名")
    private String userName;

    @Field("nickName")
    @ApiModelProperty(value="人员中文名")
    private String nickName;

    @Field("total")
    @ApiModelProperty(value="统计总行数")
    private int total;

    @Field("additions")
    @ApiModelProperty(value="新增行数")
    private int additions;

    @Field("deletions")
    @ApiModelProperty(value="删除行数")
    private int deletions;

    @Field("deatil")
    @ApiModelProperty(value="commit详情")
    private List deatil;

    @Field("committedDate")
    @ApiModelProperty(value="提交时间")
    private String committedDate;

    @Field("groupid")
    @ApiModelProperty(value="小组id")
    private String groupid;

    @Field("groupname")
    @ApiModelProperty(value="小组名")
    private String groupname;

    @Field("companyid")
    @ApiModelProperty(value="公司id")
    private String companyid;

    @Field("companyname")
    @ApiModelProperty(value="公司名称")
    private String companyname;

    private String rolename;
    private String startDate;
    private String endDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getAdditions() {
        return additions;
    }

    public void setAdditions(int additions) {
        this.additions = additions;
    }

    public int getDeletions() {
        return deletions;
    }

    public void setDeletions(int deletions) {
        this.deletions = deletions;
    }

    public List getDeatil() {
        return deatil;
    }

    public void setDeatil(List deatil) {
        this.deatil = deatil;
    }

    public String getCommittedDate() {
        return committedDate;
    }

    public void setCommittedDate(String committedDate) {
        this.committedDate = committedDate;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getCompanyid() {
        return companyid;
    }

    public void setCompanyid(String companyid) {
        this.companyid = companyid;
    }

    public String getCompanyname() {
        return companyname;
    }

    public void setCompanyname(String companyname) {
        this.companyname = companyname;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getRolename() {
        return rolename;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }
}
